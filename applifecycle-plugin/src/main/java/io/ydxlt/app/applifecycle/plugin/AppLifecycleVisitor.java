package io.ydxlt.app.applifecycle.plugin;


import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.List;


public class AppLifecycleVisitor extends ClassVisitor {

    private static final String INIT = "<init>";
    private static final String INIT_DESC = "()V";

    private List<String> appLifecycleList;

    public AppLifecycleVisitor(ClassVisitor classVisitor, List<String> appLifecycles) {
        super(Opcodes.ASM9,classVisitor);
        this.appLifecycleList = appLifecycles;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        System.out.println("AppLifecycleVisitor visitMethod name = "+name + " appLifecycleList = "+appLifecycleList);
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        if(INIT.equals(name) && INIT_DESC.equals(desc) && (access & Opcodes.ACC_PRIVATE) != 0){
            visitor = new AdviceAdapter(Opcodes.ASM9,visitor,access,name,desc){
                @Override
                protected void onMethodExit(int opcode) {
                    for(String name : appLifecycleList){
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitLdcInsn(name.replace("/","."));
                        mv.visitMethodInsn(INVOKESPECIAL, "io/ydxlt/app/lifecycle/api/AppLifecycles", "register", "(Ljava/lang/String;)V", false);
                    }
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitMethodInsn(INVOKESPECIAL, "io/ydxlt/app/lifecycle/api/AppLifecycles", "sortByPriority", "()V", false);
                }
            };
        }
        return visitor;
    }

}
