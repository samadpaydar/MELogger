package ir.ac.um.melogger.analysis.instrument;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiMethod;

public class InvocationInjector extends JavaRecursiveElementVisitor {
    private Instrumenter instrumenter;

    public InvocationInjector(Instrumenter instrumenter) {
        this.instrumenter = instrumenter;
    }

    @Override
    public void visitMethod(PsiMethod method) {
        super.visitElement(method);
        if (method.getBody() != null && method.getBody().getStatements() != null) {
            ApplicationManager.getApplication().invokeLater(new InvocationInjectorThread(instrumenter, method));
        }
    }
}
