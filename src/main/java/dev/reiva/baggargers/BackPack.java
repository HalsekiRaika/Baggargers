package dev.reiva.baggargers;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
final class BackPack extends Item {
	BackPack() {
		super(new Settings().maxCount(1));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		final var absHeldBackPack = user.getStackInHand(hand);
		final var bpUuid = getOrCreateUUID(absHeldBackPack);
		if (!user.isSneaking()) {
			if (!world.isClient()) {
				user.openHandledScreen(new ExtendedScreenHandlerFactory() {
					@Override
					public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
						buf.writeUuid(bpUuid);
					}

					@Override
					public Text getDisplayName() {
						return LiteralText.EMPTY;
					}

					@Override
					public ScreenHandler createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
						return new BackPackScreenHandler(id, playerInventory, bpUuid, absHeldBackPack);
					}
				});
			} else {
				user.playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 1.0f, 1.0f);
			}
			return TypedActionResult.consume(absHeldBackPack);
		}
		return super.use(world, user, hand);
	}

	public static UUID getOrCreateUUID(ItemStack stack) {
		var foundUid = getUuid(stack);

		if (foundUid == null) {
			return createUuid(stack);
		}

		return foundUid;
	}

	public static UUID getUuid(ItemStack stack) {
		try {
			return stack.getOrCreateNbt().getUuid("bp_uuid");
		} catch (Exception e) {
			return null;
		}
	}

	public static UUID createUuid(ItemStack stack) {
		var uuid = UUID.randomUUID();
		stack.getOrCreateNbt().putUuid("bp_uuid", uuid);

		return uuid;
	}

	public static boolean matchUuid(ItemStack stack, UUID uuid) {
		var origin = getUuid(stack);
		return origin != null && origin.equals(uuid);
	}

	static final class BackPackScreenHandler extends ScreenHandler {
		private final BackPackInventory bpInv;
		private final PlayerInventory playerInv;
		private final UUID back_id;

		private static final int HOTBAR_SLOT_COUNT = 9;
		private static final int PLAYER_INVENTORY_COL_LIMIT = 3;
		private static final int PLAYER_INVENTORY_ROW_LIMIT = 9;
		private static final int BACKPACK_INVENTORY_COL_LIMIT = 9;
		private static final int BACKPACK_INVENTORY_ROW_LIMIT = 13;

		BackPackScreenHandler(int id, PlayerInventory playerInventory, PacketByteBuf packetByteBuf) {
			this(id, playerInventory, packetByteBuf.readUuid(), Items.AIR.getDefaultStack());
		}

		BackPackScreenHandler(int id, PlayerInventory playerInv, UUID uuid, ItemStack stack) {
			super(Registries.BACK_PACK_SCREEN, id);
			this.bpInv = new BackPackInventory(stack);
			this.playerInv = playerInv;
			this.back_id = uuid;

			for (int col = 0; col < BACKPACK_INVENTORY_COL_LIMIT; col++) {
				for (int row = 0; row < BACKPACK_INVENTORY_ROW_LIMIT; row++) {
					this.addSlot(new BackPackSlot(bpInv, (col * 13) + row, 12 + row * 18, 12 + col * 18));
				}
			}

			for (int col = 0; col < PLAYER_INVENTORY_COL_LIMIT; col++) {
				for (int row = 0; row < PLAYER_INVENTORY_ROW_LIMIT; row++) {
					this.addSlot(new Slot(playerInv, row + col * 9 + 9, 48 + row * 18, 177 + col * 18));
				}
			}

			for (int row = 0; row < HOTBAR_SLOT_COUNT; row++) {
				this.addSlot(new Slot(playerInv, row, 48 + row * 18, 233));
			}
		}

		@Override
		public boolean canUse(PlayerEntity player) {
			if (player.getWorld().isClient())
				return true;
			var stack = bpInv.getStack();
			var match = BackPack.matchUuid(stack, this.back_id);

			return !stack.isEmpty() && stack.getItem() instanceof BackPack && match;
		}

		@SuppressWarnings("ConstantConditions")
		@Override
		public ItemStack transferSlot(PlayerEntity player, int index) {
			var stack = ItemStack.EMPTY;
			var slot = this.slots.get(index);

			if (slot != null && slot.hasStack()) {
				final var stack_inner = slot.getStack();
				stack = stack_inner.copy();
				if (index < this.bpInv.size()) {
					if (!this.insertItem(stack_inner, this.bpInv.size(), this.slots.size(), true)) {
						return ItemStack.EMPTY;
					}
				} else if (!this.insertItem(stack_inner, 0, this.bpInv.size(), false)) {
					return ItemStack.EMPTY;
				}

				if (stack_inner.isEmpty()) {
					slot.setStack(ItemStack.EMPTY);
				} else {
					slot.markDirty();
				}
			}

			return stack;
		}

		@Override
		public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
			if (slotIndex >= 0 && player.getInventory().selectedSlot + 27 + this.bpInv.size() == slotIndex) {
				if (actionType != SlotActionType.CLONE) {
					return;
				}
			}
			super.onSlotClick(slotIndex, button, actionType, player);
		}

		@Override
		public void close(PlayerEntity player) {
			super.close(player);
			this.bpInv.onClose(player);
		}

		static class BackPackSlot extends Slot {
			BackPackSlot(Inventory inv, int index, int x, int y) {
				super(inv, index, x, y);
			}

			@Override
			public boolean canInsert(ItemStack stack) {
				if (stack.getItem() instanceof BackPack) {
					return false;
				}

				if (stack.getItem() instanceof final BlockItem item) {
					return !(item.getBlock() instanceof ShulkerBoxBlock);
				}

				return true;
			}
		}
	}

	static final class BackPackInventory extends SimpleInventory {
		private final ItemStack itemStack;
		private static final int INV_SIZE = 117;
		private static final String TAG_NAME = "bp_contents";
		private static final String TAG_CONTENTS_NAME = "items";

		BackPackInventory(ItemStack itemStack) {
			super(getStacks(itemStack).toArray(new ItemStack[INV_SIZE]));
			this.itemStack = itemStack;
		}

		@Override
		public void markDirty() {
			super.markDirty();
			var beTag = itemStack.getSubNbt(NBTTag());
			if (beTag == null)
				beTag = itemStack.getOrCreateSubNbt(NBTTag());

			if (this.isEmpty()) {
				if (beTag.contains(TAG_CONTENTS_NAME))
					beTag.remove(TAG_CONTENTS_NAME);
			} else {
				DefaultedList<ItemStack> stacks = DefaultedList.ofSize(INV_SIZE, ItemStack.EMPTY);
				for (int i = 0; i < this.size(); i++) {
					stacks.set(i, this.getStack(i));
				}
				Inventories.writeNbt(beTag, stacks);
			}

			if (beTag.contains(TAG_CONTENTS_NAME) ? beTag.getKeys().size() == 0 : this.isEmpty()) {
				itemStack.removeSubNbt(NBTTag());
			}
		}

		@Override
		public void onClose(PlayerEntity player) {
			if (itemStack.getCount() > 1) {
				final int count = itemStack.getCount();
				itemStack.setCount(1);
				player.giveItemStack(new ItemStack(itemStack.getItem(), count - 1));
			}
			markDirty();
		}

		public ItemStack getStack() {
			return this.itemStack;
		}

		public static String NBTTag() {
			return TAG_NAME;
		}

		public static DefaultedList<ItemStack> getStacks(ItemStack stackIn) {
			var bpTag = stackIn.getSubNbt(NBTTag());
			var stacks = DefaultedList.ofSize(INV_SIZE, ItemStack.EMPTY);
			if (bpTag != null && bpTag.contains("Items", NbtElement.LIST_TYPE)) {
				Inventories.readNbt(bpTag, stacks);
			}
			return stacks;
		}
	}
}
