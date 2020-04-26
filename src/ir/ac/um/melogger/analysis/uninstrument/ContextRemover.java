package ir.ac.um.melogger.analysis.uninstrument;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import ir.ac.um.melogger.analysis.instrument.ContextInjectorThread;
import ir.ac.um.melogger.analysis.instrument.Instrumenter;

public class ContextRemover extends JavaRecursiveElementVisitor {
    private UnInstrumenter unInstrumenter;
    private String applicationClassName;
    private boolean done = false;

    public ContextRemover(UnInstrumenter unInstrumenter, String applicationClassName) {
        this.unInstrumenter = unInstrumenter;
        this.applicationClassName = applicationClassName;
    }

    @Override
    public void visitClass(PsiClass aClass) {
        super.visitClass(aClass);
        String className = aClass.getQualifiedName();
        //check for anonymous classes
        if (!done && className != null && className.equals(applicationClassName) && aClass.isWritable()) {
            done = true;
            ApplicationManager.getApplication().invokeLater(new ContextRemoverThread(this.unInstrumenter, aClass));
        }
    }

}
