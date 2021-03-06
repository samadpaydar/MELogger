package ir.ac.um.melogger.analysis.instrument;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import ir.ac.um.melogger.analysis.utils.AnalysisUtils;

public class InvocationInjectorThread implements Runnable {
    private Instrumenter instrumenter;
    private PsiMethod method;

    public InvocationInjectorThread(Instrumenter instrumenter, PsiMethod method) {
        this.instrumenter = instrumenter;
        this.method = method;
    }

    @Override
    public void run() {
        WriteCommandAction.runWriteCommandAction(instrumenter.getProject(), () -> {
            //skip instrumenting the logger method that is added by this plugin.
            if (AnalysisUtils.LOGGER_CLASS_QUALIFIED_NAME.equals(method.getContainingClass().getQualifiedName())) {
                return;
            }
            //skip instrumenting the context getter method that is added to the application class
            if (AnalysisUtils.CONTEXT_GETTER_METHOD_NAME.equals(method.getName())) {
                return;
            }
            PsiCodeBlock body = method.getBody();
            if (body != null && body.getStatements() != null && body.getStatements().length > 0) {
                final JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(instrumenter.getProject());
                final PsiElementFactory factory = psiFacade.getElementFactory();
                PsiStatement firstBodyStatement = body.getStatements()[0];
                String statementText = String.format(AnalysisUtils.LOG_STATEMENT_TEMPLATE, AnalysisUtils.getMethodQualifiedName(method));
                PsiStatement logStatement = factory.createStatementFromText(statementText, firstBodyStatement);
                boolean invokesThisOrSuper = method.isConstructor() ?
                        AnalysisUtils.isFirstStatementThisOrSuper(method) : false;
                PsiStatement statement = invokesThisOrSuper ? (body.getStatements().length > 2 ? body.getStatements()[1] : null)
                        : (body.getStatements()[0]);
                if (statement != null && statement.getText().trim().equals(logStatement.getText().trim())) {
                    //the statement is already added
                    return;
                }
                final PsiElement addedStatement = invokesThisOrSuper ?
                        body.addAfter(logStatement, firstBodyStatement) :
                        body.addBefore(logStatement, firstBodyStatement);
                final CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(instrumenter.getProject());
                codeStyleManager.reformat(addedStatement);
            }
        });
    }
}
