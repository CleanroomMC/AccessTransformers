package net.minecraftforge.accesstransformer.parser;

import net.minecraftforge.accesstransformer.*;
import net.minecraftforge.accesstransformer.generated.AtParser;
import net.minecraftforge.accesstransformer.generated.AtParserBaseVisitor;
import org.antlr.v4.runtime.RuleContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class AccessTransformVisitor extends AtParserBaseVisitor<Void> {

    private final INameHandler nameHandler;
    private final String origin;
    private final List<AccessTransformer> accessTransformers = new ArrayList<>();

    public AccessTransformVisitor(final String origin, final INameHandler nameHandler) {
        this.origin = origin;
        this.nameHandler = nameHandler;
    }

    @Override
    public Void visitEntry(final AtParser.EntryContext ctx) {
        if (ctx.line_value() == null) {
            String className = ctx.class_name().getText();
            String modifier = ctx.keyword().getText();
            className = nameHandler.translateClassName(className);
            Target<?> target = new ClassTarget(className);
            accessTransformers.add(new AccessTransformer(target,
                    ModifierProcessor.modifier(modifier),
                    ModifierProcessor.finalState(modifier),
                    this.origin,
                    ctx.getStart().getLine()));

            int idx = className.lastIndexOf('$'); // Java uses this to identify inner classes, Scala/others use it for synthetics. Either way we should be fine as it will skip
            // over classes that don't exist.
            if (idx != -1) {
                String parent = className.substring(0, idx);
                accessTransformers.add(new AccessTransformer(new InnerClassTarget(parent, className.replace('.', '/')),
                        ModifierProcessor.modifier(modifier),
                        ModifierProcessor.finalState(modifier),
                        this.origin,
                        ctx.getStart().getLine()));
            }
        }
        return super.visitEntry(ctx);
    }

    @Override
    public Void visitWildcard_method(final AtParser.Wildcard_methodContext ctx) {
        final AtParser.EntryContext entry = (AtParser.EntryContext) ctx.getParent().getParent();
        String className = entry.class_name().getText();
        String modifier = entry.keyword().getText();
        Target<?> target = new WildcardTarget(className, true);
        accessTransformers.add(new AccessTransformer(target, ModifierProcessor.modifier(modifier), ModifierProcessor.finalState(modifier), this.origin, ctx.getStart().getLine()));
        return super.visitWildcard_method(ctx);
    }

    @Override
    public Void visitWildcard_field(final AtParser.Wildcard_fieldContext ctx) {
        final AtParser.EntryContext entry = (AtParser.EntryContext) ctx.getParent().getParent();
        String className = entry.class_name().getText();
        String modifier = entry.keyword().getText();
        Target<?> target = new WildcardTarget(className, false);
        accessTransformers.add(new AccessTransformer(target, ModifierProcessor.modifier(modifier), ModifierProcessor.finalState(modifier), this.origin, ctx.getStart().getLine()));
        return super.visitWildcard_field(ctx);
    }

    @Override
    public Void visitField_name(final AtParser.Field_nameContext ctx) {
        final AtParser.EntryContext entry = (AtParser.EntryContext) ctx.getParent().getParent();
        String className = entry.class_name().getText();
        String modifier = entry.keyword().getText();
        String fieldName = ctx.getText();
        className = nameHandler.translateClassName(className);
        fieldName = nameHandler.translateFieldName(fieldName);
        Target<?> target = new FieldTarget(className, fieldName);
        accessTransformers.add(new AccessTransformer(target, ModifierProcessor.modifier(modifier), ModifierProcessor.finalState(modifier), this.origin, ctx.getStart().getLine()));
        return super.visitField_name(ctx);
    }

    @Override
    public Void visitFunction(final AtParser.FunctionContext ctx) {
        final AtParser.EntryContext entry = (AtParser.EntryContext) ctx.getParent().getParent();
        String className = entry.class_name().getText();
        String modifier = entry.keyword().getText();
        String methodName = ctx.func_name().getText();
        className = nameHandler.translateClassName(className);
        methodName = nameHandler.translateMethodName(methodName);
        List<String> args = ctx.argument().stream().map(RuleContext::getText).collect(Collectors.toList());
        String retVal = ctx.return_value().getText();
        Target<?> target = new MethodTarget(className, methodName, args, retVal);
        accessTransformers.add(new AccessTransformer(target, ModifierProcessor.modifier(modifier), ModifierProcessor.finalState(modifier), this.origin, ctx.getStart().getLine()));
        return super.visitFunction(ctx);
    }

    public List<AccessTransformer> getAccessTransformers() {
        return accessTransformers;
    }

}
