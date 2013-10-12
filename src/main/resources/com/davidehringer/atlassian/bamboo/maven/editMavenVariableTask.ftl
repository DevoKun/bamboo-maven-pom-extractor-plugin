
[@ww.select labelKey='maven.extractor.config.options' name='gavOrCustom'
                                        listKey='key' listValue='value' toggle='true'
                                        list=options /]
                                        
[@ui.bambooSection dependsOn="gavOrCustom" showOn="0" titleKey='maven.extractor.config.gav']
        [@ww.radio labelKey='maven.extractor.config.option.prefix' name='prefixOption'
                   listKey='key' listValue='value' toggle='true'
                   list=prefixOptions ]
        [/@ww.radio]
        [@ui.bambooSection dependsOn='prefixOption' showOn='0']
            [@ww.textfield labelKey='maven.extractor.config.option.prefix.custom.field' name='customPrefix' /]
        [/@ui.bambooSection]
[/@ui.bambooSection]
                                        
[@ui.bambooSection dependsOn='gavOrCustom' showOn='1' titleKey='maven.extractor.config.custom']
        [@ww.textfield labelKey='maven.extractor.config.custom.variable.name' name='customVariableName' required='true' cssClass="long-field" /]
        [@ww.textfield labelKey='maven.extractor.config.custom.element' name='customElement' required='true' cssClass="long-field" /]
[/@ui.bambooSection]

[@ui.bambooSection titleKey='maven.extractor.config.pom.options']
	[@ww.textfield labelKey='builder.maven2.projectFile' name='projectFile' cssClass="long-field" /]
[/@ui.bambooSection]