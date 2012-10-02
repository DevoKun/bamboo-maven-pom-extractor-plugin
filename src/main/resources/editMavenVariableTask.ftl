[@ww.textfield labelKey="com.davidehringer.atlassian.bamboo.maven.say" name="say" required='true'/]

[@ww.select labelKey='config.options' name='gavOrCustom'
                                        listKey='key' listValue='value' toggle='true'
                                        list=options /]
                                        
[@ui.bambooSection dependsOn='gavOrCustom' showOn='1' titleKey='config.custom']
        [@ww.textfield labelKey='custom.element.0' name='custom.element.0' required='true' cssClass="long-field" /]
        [@ww.textfield labelKey='custom.element.1' name='custom.element.1' cssClass="long-field" /]
        [@ww.password  name='custom.element.2' cssClass="long-field" /]
[/@ui.bambooSection]

[@ww.textfield labelKey='builder.maven2.projectFile' name='projectFile' cssClass="long-field" /]
