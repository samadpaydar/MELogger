package ir.ac.um.melogger.analysis.uninstrument;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import ir.ac.um.melogger.analysis.utils.AnalysisUtils;

public class InvocationRemover extends JavaRecursiveElementVisitor {
    private UnInstrumenter unInstrumenter;

    public InvocationRemover(UnInstrumenter unInstrumenter) {
        this.unInstrumenter = unInstrumenter;
    }

    @Override
    public void visitMethod(PsiMethod method) {
        super.visitElement(method);
        if (method.getBody() != null && method.getBody().getStatements() != null) {
            ApplicationManager.getApplication().invokeLater(new InvocationRemoverThread(this.unInstrumenter, method));
        }
    }
}