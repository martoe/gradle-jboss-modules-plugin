package com.zhurlik.descriptor
import com.zhurlik.Ver
import com.zhurlik.extension.JBossModule
import groovy.xml.MarkupBuilder

import javax.xml.transform.stream.StreamSource

import static com.zhurlik.Ver.V_1_0
import static java.io.File.separator
/**
 * Generates a xml descriptor for JBoss Module ver.1.0
 * https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_0.xsd
 *
 * @author zhurlik@gmail.com
 */
class Xsd1_0 extends Builder<JBossModule> {

    @Override
    String getXmlDescriptor(final JBossModule jmodule) {
        assert jmodule != null, 'JBossModule is null'
        assert (jmodule.moduleName != null || jmodule.moduleConfiguration) , 'Module name is null'

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        writeXmlDeclaration(xml)

        if (jmodule.isModuleConfiguration()) {
            assert jmodule.defaultLoader != null, 'Default-Loader is null'
            // todo:  <xsd:element name="configuration" type="configurationType">
            xml.configuration([xmlns: 'urn:jboss:module:' + getVersion().number, 'default-loader': jmodule.defaultLoader]) {
                if (jmodule.loaders.empty) {
                    loader([name: jmodule.defaultLoader])
                }

                jmodule.loaders.each { l ->
                    if (l instanceof String) {
                        loader([name: l])
                    } else {
                        loader([name: l.name]) {
                            if (l['import'] != null) {
                                'import'(l['import'])
                            }

                            if (l['module-path'] != null) {
                                'module-path'([name: l['module-path']])
                            }
                        }
                    }
                }
            }
        } else {
            // <module xmlns="urn:jboss:module:1.0" name="org.jboss.msc">
            xml.module([xmlns: 'urn:jboss:module:' + getVersion().number, name: jmodule.moduleName] + ((jmodule.slot in [null, '']) ? [:] : [slot: jmodule.slot])) {

                writeExports(jmodule, xml)
                writeMainClass(jmodule, xml)
                writeResources(jmodule, xml)
                writeDependencies(jmodule, xml)
            }
        }

        return writer.toString()
    }

    @Override
    JBossModule makeModule(String txt) {

        return super.makeModule(txt)
    }

    @Override
    StreamSource getXsd() {
        return new StreamSource(getClass().classLoader.getResourceAsStream(getVersion().xsd))
    }

    @Override
    String getPath(final JBossModule jbModule) {
        return ['modules', jbModule.moduleName.replaceAll('\\.', separator), ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot)].join(separator)
    }

    @Override
    protected Ver getVersion() {
        return V_1_0
    }
}
