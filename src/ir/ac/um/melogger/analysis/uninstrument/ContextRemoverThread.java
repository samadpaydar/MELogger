package ir.ac.um.melogger.analysis.uninstrument;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import ir.ac.um.melogger.analysis.instrument.Instrumenter;
import ir.ac.um.melogger.analysis.utils.AnalysisUtils;
import ir.ac.um.melogger.utils.Utils;

public class ContextRemoverThread implements Runnable {
    private UnInstrumenter unInstrumenter;
    private PsiClass aClass;

    public ContextRemoverThread(UnInstrumenter unInstrumenter, PsiClass aClass) {
        this.unInstrumenter = unInstrumenter;
        this.aClass = aClass;
    }

    @Override
    public void run() {
        WriteCommandAction.runWriteCommandAction(unInstrumenter.getProject(), () -> {
            for (PsiMethod method : aClass.getMethods()) {
                if (AnalysisUtils.CONTEXT_GETTER_METHOD_NAME.equals(method.getName())) {
                    method.delete();
                    final CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(unInstrumenter.getProject());
                    codeStyleManager.reformat(aClass);
                }
            }
            unInstrumenter.contextRemoved(aClass.getQualifiedName());
        });
    }

}
