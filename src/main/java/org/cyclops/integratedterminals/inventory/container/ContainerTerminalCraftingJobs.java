package org.cyclops.integratedterminals.inventory.container;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.part.PartStateEmpty;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.RegistryEntries;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingPlan;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.TerminalStorageTabIngredientCraftingHandlers;
import org.cyclops.integratedterminals.part.PartTypeTerminalCraftingJob;

import java.util.List;
import java.util.Optional;

/**
 * Container for the crafting jobs overview gui.
 * @author rubensworks
 */
public class ContainerTerminalCraftingJobs extends ContainerMultipart<PartTypeTerminalCraftingJob, PartStateEmpty<PartTypeTerminalCraftingJob>> {

    private final LazyOptional<INetwork> network;
    private final int valueIdCraftingJobs;

    private long lastUpdate;
    private List<HandlerWrappedTerminalCraftingPlan> craftingJobs;

    public ContainerTerminalCraftingJobs(int id, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
        this(id, playerInventory, PartHelpers.readPartTarget(packetBuffer), Optional.empty(), PartHelpers.readPart(packetBuffer));
    }

    public ContainerTerminalCraftingJobs(int id, PlayerInventory playerInventory,
                                         PartTarget target, Optional<IPartContainer> partContainer,
                                         PartTypeTerminalCraftingJob partType) {
        super(RegistryEntries.CONTAINER_PART_TERMINAL_CRAFTING_JOBS, id, playerInventory, new Inventory(), Optional.of(target), partContainer, partType);

        this.network = getTarget()
                .map(t -> NetworkHelpers.getNetwork(t.getCenter()))
                .orElse(LazyOptional.empty());

        this.lastUpdate = 0;
        this.craftingJobs = Lists.newArrayList();
        this.valueIdCraftingJobs = getNextValueId();
    }

    public LazyOptional<INetwork> getNetwork() {
        return network;
    }

    public int getChannel() {
        return IPositionedAddonsNetwork.WILDCARD_CHANNEL;
    }

    public int getValueIdCraftingJobs() {
        return valueIdCraftingJobs;
    }

    public List<HandlerWrappedTerminalCraftingPlan> getCraftingJobs() {
        return craftingJobs;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if (!this.getWorld().isRemote()
                && this.lastUpdate < System.currentTimeMillis()) {
            getNetwork().ifPresent(network -> {
                this.lastUpdate = System.currentTimeMillis() + GeneralConfig.guiTerminalCraftingJobsUpdateFrequency;

                // Load crafting jobs
                int channel = getChannel();
                this.craftingJobs = Lists.newArrayList();
                for (ITerminalStorageTabIngredientCraftingHandler<?, ?> handler : TerminalStorageTabIngredientCraftingHandlers.REGISTRY.getHandlers()) {
                    for (ITerminalCraftingPlan craftingJob : handler.getCraftingJobs(network, channel)) {
                        this.craftingJobs.add(new HandlerWrappedTerminalCraftingPlan(handler, craftingJob));
                    }
                }

                // Send crafting jobs to client
                ListNBT tagList = new ListNBT();
                for (HandlerWrappedTerminalCraftingPlan craftingJob : this.craftingJobs) {
                    tagList.add(HandlerWrappedTerminalCraftingPlan.serialize(craftingJob));
                }
                CompoundNBT tag = new CompoundNBT();
                tag.put("craftingJobs", tagList);
                setValue(this.valueIdCraftingJobs, tag);
            });
        }
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    @Override
    public void onUpdate(int valueId, CompoundNBT value) {
        super.onUpdate(valueId, value);

        if (valueId == this.valueIdCraftingJobs) {
            ListNBT tagList = value.getList("craftingJobs", Constants.NBT.TAG_COMPOUND);
            this.craftingJobs = Lists.newArrayListWithExpectedSize(tagList.size());
            for (int i = 0; i < tagList.size(); i++) {
                this.craftingJobs.add(HandlerWrappedTerminalCraftingPlan.deserialize(tagList.getCompound(i)));
            }
        }
    }

}
