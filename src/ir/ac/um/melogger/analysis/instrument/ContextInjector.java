package ir.ac.um.melogger.analysis.instrument;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;

public class ContextInjector extends JavaRecursiveElementVisitor {
    private Instrumenter instrumenter;
    private String applicationClassName;
    private boolean done = false;

    public ContextInjector(Instrumenter instrumenter, String applicationClassName) {
        this.instrumenter = instrumenter;
        this.applicationClassName = applicationClassName;
    }

    @Override
    public void visitClass(PsiClass aClass) {
        super.visitClass(aClass);
        String className = aClass.getQualifiedName();
        //check for anonymous classes
        if (!done && className != null && className.equals(applicationClassName) && aClass.isWritable()) {
            done = true;
            ApplicationManager.getApplication().invokeLater(new ContextInjectorThread(this.instrumenter, aClass));
        }
    }

}
