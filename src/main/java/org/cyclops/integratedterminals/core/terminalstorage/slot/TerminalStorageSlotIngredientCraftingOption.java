package org.cyclops.integratedterminals.core.terminalstorage.slot;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingOption;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;

import javax.annotation.Nullable;
import java.util.List;

/**
 * An ingredient slot for a crafting option.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class TerminalStorageSlotIngredientCraftingOption<T, M> extends TerminalStorageSlotIngredient<T, M> {

    private final HandlerWrappedTerminalCraftingOption<T> craftingOption;

    public TerminalStorageSlotIngredientCraftingOption(IIngredientComponentTerminalStorageHandler<T, M> ingredientComponentViewHandler, T instance, HandlerWrappedTerminalCraftingOption<T> craftingOption) {
        super(ingredientComponentViewHandler, instance);
        this.craftingOption = craftingOption;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawGuiContainerLayer(AbstractContainerScreen gui, PoseStack matrixStack, ContainerScreenTerminalStorage.DrawLayer layer,
                                      float partialTick, int x, int y, int mouseX, int mouseY,
                                      ITerminalStorageTabClient tab, int channel, @Nullable String label) {
        IIngredientComponentTerminalStorageHandler<T, M> viewHandler = getIngredientComponentViewHandler();
        if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND) {
            long maxQuantity = ((TerminalStorageTabIngredientComponentClient) tab).getMaxQuantity(channel);
            viewHandler.drawInstance(matrixStack, getInstance(), maxQuantity, null, gui, layer, partialTick, x, y, mouseX, mouseY, null);
            drawCraftLabel(x, y);
        } else {
            long maxQuantity = ((TerminalStorageTabIngredientComponentClient) tab).getMaxQuantity(channel);
            getIngredientComponentViewHandler().drawInstance(matrixStack, getInstance(), maxQuantity, label, gui, layer, partialTick, x, y, mouseX, mouseY, getTooltipLines());
        }
    }

    protected List<Component> getTooltipLines() {
        List<Component> tooltipLines = Lists.newArrayList();
        tooltipLines.add(Component.translatable("gui.integratedterminals.terminal_storage.tooltip.requirements")
                .withStyle(ChatFormatting.YELLOW));
        ITerminalCraftingOption<T> option = getCraftingOption().getCraftingOption();
        for (IngredientComponent<?, ?> inputComponent : option.getInputComponents()) {
            IIngredientMatcher matcher = inputComponent.getMatcher();
            for (Object inputInstance : option.getInputs(inputComponent)) {
                if (!matcher.isEmpty(inputInstance)) {
                    tooltipLines.add(Component.literal(String.format("%s- %s (%s)",
                            ChatFormatting.GRAY, matcher.localize(inputInstance), matcher.getQuantity(inputInstance))));
                }
            }
        }
        return tooltipLines;
    }

    public HandlerWrappedTerminalCraftingOption<T> getCraftingOption() {
        return craftingOption;
    }

    private void drawCraftLabel(int x, int y) {
        RenderItemExtendedSlotCount.getInstance().drawSlotText(Minecraft.getInstance().font, new PoseStack(),
                ChatFormatting.GOLD + L10NHelpers.localize("gui.integratedterminals.terminal_storage.craft"), x, y - 11);
    }

}
