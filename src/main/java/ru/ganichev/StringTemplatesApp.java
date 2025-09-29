package ru.ganichev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class StringTemplatesApp {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) throws IOException {
        new StringTemplatesApp().applyUserExceptionDeclarationConversion().applyThrowBlockConversion().applyTryBlockConversion();
    }

    private StringTemplatesApp applyTryBlockConversion() {
        var tryBlockTemplate = getTemplate("templates/tryBlockTemplate.stg", "tryBlockTemplate");
        var ifStatementTemplate = getTemplate("templates/ifStatementTemplate.stg", "ifStatementTemplate");
        var elseIfPartTemplate1 = getTemplate("templates/elseIfPartTemplate.stg", "elseIfPartTemplate");
        var elseIfPartTemplate2 = getTemplate("templates/elseIfPartTemplate.stg", "elseIfPartTemplate");
        var elsePartTemplate = getTemplate("templates/elsePartTemplate.stg", "elsePartTemplate");
        ifStatementTemplate.add("expressionElement", List.of("err.isExceptionEquals(MY_USER_DEFINED_EXCEPTION1)"));
        ifStatementTemplate.add("statementElement", List.of("log.warn(\"MyUserDefinedException1 thrown\");"));
        elseIfPartTemplate1.add("expressionElement", List.of("err.isExceptionEquals(MY_USER_DEFINED_EXCEPTION2)"));
        elseIfPartTemplate1.add("statementElement", List.of("log.warn(\"MyUserDefinedException2 thrown\");"));
        elseIfPartTemplate2.add("expressionElement", List.of("err.isExceptionEquals(MY_USER_DEFINED_EXCEPTION3)"));
        elseIfPartTemplate2.add("statementElement", List.of("log.warn(\"MyUserDefinedException3 thrown\");"));
        elsePartTemplate.add("statementElement", List.of("log.warn(\"Unexpected error\");"));
        ifStatementTemplate.add("elseIfPartElement", List.of(elseIfPartTemplate1, elseIfPartTemplate2));
        ifStatementTemplate.add("elsePartElement", elsePartTemplate);
        tryBlockTemplate.add("mainStatementElement", "throw new ApplicationException(MY_USER_DEFINED_EXCEPTION1);");
        tryBlockTemplate.add("ifElement", ifStatementTemplate);
        log.info(tryBlockTemplate.render());
        return this;
    }

    private StringTemplatesApp applyUserExceptionDeclarationConversion() {
        var variableName = getTemplate("templates/variableName.stg", "variableNameTemplate");
        var declareUserDefinedException = getTemplate("templates/declareUserDefinedExceptionTemplate.stg", "declareUserDefinedExceptionTemplate");
        variableName.add("name", "MY_USER_DEFINED_EXCEPTION1");
        declareUserDefinedException.add("exceptionVariableName", variableName);
        declareUserDefinedException.add("errorCode", -20001);
        log.info(declareUserDefinedException.render());
        return this;
    }

    private StringTemplatesApp applyThrowBlockConversion() {
        var variableName = getTemplate("templates/variableName.stg", "variableNameTemplate");
        var throwTemplate = getTemplate("templates/throwTemplate.stg", "throwTemplate");
        variableName.add("name", "MY_USER_DEFINED_EXCEPTION1");
        throwTemplate.add("exceptionVariableName", variableName);
        log.info(throwTemplate.render());
        return this;
    }

    private ST getTemplate(String filePath, String templateName) {
        URL resourceUrl = StringTemplatesApp.class.getClassLoader().getResource(filePath);
        if (resourceUrl == null) {
            throw new IllegalStateException("Файл шаблона не найден!");
        }
        STGroupFile group = new STGroupFile(resourceUrl);
        return group.getInstanceOf(templateName);
    }
}