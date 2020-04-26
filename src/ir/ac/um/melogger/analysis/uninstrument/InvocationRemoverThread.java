package ir.ac.um.melogger.analysis.uninstrument;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import ir.ac.um.melogger.analysis.utils.AnalysisUtils;

public class InvocationRemoverThread implements Runnable {
    private UnInstrumenter unInstrumenter;
    private PsiMethod method;

    public InvocationRemoverThread(UnInstrumenter unInstrumenter, PsiMethod method) {
        this.unInstrumenter = unInstrumenter;
        this.method = method;
    }

    @Override
    public void run() {
        WriteCommandAction.runWriteCommandAction(unInstrumenter.getProject(), () -> {
            PsiCodeBlock body = method.getBody();
            if (body != null && body.getStatements() != null && body.getStatements().length > 0) {
                PsiStatement firstBodyStatement = body.getStatements()[0];
                final JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(unInstrumenter.getProject());
                final PsiElementFactory factory = psiFacade.getElementFactory();
                String statementText = String.format(AnalysisUtils.LOG_STATEMENT_TEMPLATE, AnalysisUtils.getMethodQualifiedName(method));
                PsiStatement logStatement = factory.createStatementFromText(statementText, firstBodyStatement);
                boolean invokesThisOrSuper = method.isConstructor() ? AnalysisUtils.isFirstStatementThisOrSuper(method) : false;
                PsiStatement logStatement2 = invokesThisOrSuper ? body.getStatements()[1] : body.getStatements()[0];
                if (logStatement.getText().equals(logStatement2.getText())) {
                    logStatement2.delete();
                    final CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(unInstrumenter.getProject());
                    codeStyleManager.reformat(body);
                }
            }
        });
    }
}
