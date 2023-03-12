package fr.redstonneur1256.lsl.mixins;

import arc.Core;
import arc.func.Cons;
import arc.scene.Element;
import arc.scene.ui.Button;
import arc.scene.ui.Dialog;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.SnapshotSeq;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.JoinDialog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// make sure to inject after ModLib's mixins so that the add button is before the refresh button
@Mixin(value = JoinDialog.class, priority = 500)
public abstract class JoinDialogMixin {

    private static final int PADDING = 3;

    @Shadow
    Dialog add;
    @Shadow
    JoinDialog.Server renaming;

    private int getServersPerRow() {
        return Core.settings.getInt("lsl.serversPerRow", 3);
    }

    private float getTargetWidth() {
        return Core.graphics.getWidth() / Scl.scl() * 0.9f;
    }

    private float getServerWidth() {
        return getTargetWidth() / getServersPerRow() - 2 * PADDING;
    }

    @Inject(method = "setup",
            at = @At(value = "INVOKE", target = "Larc/scene/ui/layout/Table;row()Larc/scene/ui/layout/Table;", shift = At.Shift.BEFORE, ordinal = 1),
            cancellable = true)
    private void removeDefaultAddButton(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(
            method = "*(Ljava/lang/String;ZLarc/scene/ui/layout/Collapser;Larc/scene/ui/layout/Table;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Larc/scene/ui/layout/Table;button(Larc/scene/style/Drawable;Larc/scene/ui/ImageButton$ImageButtonStyle;Ljava/lang/Runnable;)Larc/scene/ui/layout/Cell;",
                    ordinal = 1
            )
    )
    private void injectAddServerButton(String label, boolean eye, Collapser coll, Table name, CallbackInfo ci) {
        if(!label.equals("@servers.remote")) {
            return;
        }

        name.button(Icon.add, Styles.emptyi, () -> {
            renaming = null;
            add.show();
        }).size(40f).right().padRight(3).tooltip("@server.add");
    }

    @Inject(method = "targetWidth", at = @At("HEAD"), cancellable = true)
    private void targetWidth(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(getTargetWidth());
    }

    @Redirect(
            method = { "setupRemote", "addCommunityHost", "buildServer" },
            at = @At(value = "INVOKE", target = "Lmindustry/ui/dialogs/JoinDialog;targetWidth()F")
    )
    private float redirectBuildServerWidth(JoinDialog instance) {
        return getServerWidth();
    }

    @Redirect(method = "setupRemote", at = @At(value = "INVOKE", target = "Larc/scene/ui/layout/Table;button(Larc/func/Cons;Larc/scene/ui/Button$ButtonStyle;Ljava/lang/Runnable;)Larc/scene/ui/layout/Cell;"))
    private Cell<Button> redirectSetupRemoteButton(Table container, Cons<Button> cons, Button.ButtonStyle style, Runnable listener) {
        return redirectButton(container, cons, style, listener, 0);
    }

    @Redirect(method = "addCommunityHost", at = @At(value = "INVOKE", target = "Larc/scene/ui/layout/Table;button(Larc/func/Cons;Larc/scene/ui/Button$ButtonStyle;Ljava/lang/Runnable;)Larc/scene/ui/layout/Cell;"))
    private Cell<Button> redirectAddCommunityHostButton(Table container, Cons<Button> cons, Button.ButtonStyle style, Runnable listener) {
        return redirectButton(container, cons, style, listener, 1);
    }

    private Cell<Button> redirectButton(Table container, Cons<Button> cons, Button.ButtonStyle style, Runnable listener, int minimumElements) {
        SnapshotSeq<Element> children = container.getChildren();

        boolean createNewRow = children.size <= minimumElements || ((Table) children.peek()).getChildren().size == getServersPerRow();

        Table table = createNewRow ? container.table().width(getTargetWidth()).get() : (Table) children.peek();
        if(createNewRow) {
            container.row();
        }

        return table.button(cons, style, listener).growY();
    }

    @Redirect(method = "addCommunityHost", at = @At(value = "INVOKE", target = "Larc/scene/ui/layout/Cell;row()V"))
    private void addCommunityHostRow(Cell<?> instance) {
        instance.pad(PADDING);
    }

    @Redirect(method = "buildServer", at = @At(value = "INVOKE", target = "Larc/scene/ui/layout/Cell;growX()Larc/scene/ui/layout/Cell;", ordinal = 1))
    private Cell<?> redirectBuildServerGrowY(Cell<?> instance) {
        return instance.grow();
    }

}
