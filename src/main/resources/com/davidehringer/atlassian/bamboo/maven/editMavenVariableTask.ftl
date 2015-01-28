
[@ui.bambooSection titleKey='maven.extractor.config.general']
	[@ww.select labelKey='maven.extractor.config.options' name='gavOrCustom'
	                                        listKey='key' listValue='value' toggle='true'
	                                        list=options /]
	[@ww.radio labelKey='maven.extractor.config.option.variableType' name='variableType'
	           listKey='key' listValue='value' toggle='true'
	           list=variableTypeOptions ]
	[/@ww.radio]
[/@ui.bambooSection]
                                        
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

<script type="text/javascript">
(function ($) {

	var helpIcon = $("#variableContextHelp");
	var helpDialog = AJS.InlineDialog(helpIcon, "contextHelpDialog",
		    function(content, trigger, showPopup) {
		        content.css({"padding":"20px"}).html('<b>Job</b> scoped variables have local scope and they cease to exist when the job finishes.<br/><br/><b>Result</b> scoped variables persist beyond the execution of a Job and are passed into subsequent stages or related deployment releases.<br/><br/><b>Plan</b> variables are visible to any jobs and stages in the Plan. Any new plan variables you set or any changes to plan variables that you make within the context of that build will not take effect until the next build executes. Plan variables can only be set for Build Plans and not Deployment Projects.</dd></dl>');
		        showPopup();
		        return false;
		    }
		);

}(jQuery));
</script>
