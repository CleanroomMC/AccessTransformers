package net.minecraftforge.accesstransformer;

import org.objectweb.asm.Type;

import java.util.Objects;
import java.util.Set;

public abstract class Target<T> {

    private final String className;
    private final Type type;

    public Target(String className) {
        this.className = className;
        this.type = Type.getType("L" + className.replaceAll("\\.", "/") + ";");
    }

    public TargetType getType() {
        return TargetType.CLASS;
    }

    public String getClassName() {
        return className;
    }

    public final Type getASMType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClassName(), getType(), "CLASS");
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Target)) {return false;}
        return Objects.equals(className, ((Target<?>) obj).className) && Objects.equals(getType(), ((Target<?>) obj).getType());
    }

    @Override
    public String toString() {
        return className + " " + getType();
    }

    public abstract String targetName();

    public abstract void apply(final T node, final AccessTransformer.Modifier targetAccess, final AccessTransformer.FinalState targetFinalState, Set<String> privateChanged);

}
