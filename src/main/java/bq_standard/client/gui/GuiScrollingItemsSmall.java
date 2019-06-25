package bq_standard.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.lists.GuiScrollingBase;
import betterquesting.api.client.gui.lists.IScrollingEntry;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.RenderUtils;

public class GuiScrollingItemsSmall extends GuiScrollingBase<GuiScrollingItemsSmall.ScrollingEntryItem>
{
	private final Minecraft mc;
	
	public GuiScrollingItemsSmall(Minecraft mc, int x, int y, int w, int h)
	{
		super(mc, x, y, w, h);
		this.mc = mc;
		this.allowDragScroll(true);
	}
	
	public void addItem(BigItemStack stack)
	{
		addItem(stack, stack.getBaseStack().getDisplayName());
	}
	
	public void addItem(BigItemStack stack, String description)
	{
		this.getEntryList().add(new ScrollingEntryItem(mc, stack, description));
	}
	
	public static class ScrollingEntryItem extends GuiElement implements IScrollingEntry
	{
		private final Minecraft mc;
		private BigItemStack stack;
		private String desc = "";
		
		private List<ItemStack> subStacks = new ArrayList<ItemStack>();
		
		public ScrollingEntryItem(Minecraft mc, BigItemStack stack, String desc)
		{
			this.mc = mc;
			this.stack = stack;
			
			this.setDescription(desc);
			
			if(stack == null)
			{
				return;
			}
			
			if(stack.oreDict != null && stack.oreDict.length() > 0)
			{
				for(ItemStack oreStack : OreDictionary.getOres(stack.oreDict))
				{
					if(oreStack == null)
					{
						continue;
					}
					
					Item oItem = oreStack.getItem();
					
					List<ItemStack> tmp = new ArrayList<ItemStack>();
					
					if(oreStack.getItemDamage() == OreDictionary.WILDCARD_VALUE)
					{
						oItem.getSubItems(oItem, CreativeTabs.tabAllSearch, tmp);
					}
					
					if(tmp.size() <= 0)
					{
						if(!subStacks.contains(oreStack))
						{
							subStacks.add(oreStack.copy());
						}
					} else
					{
						for(ItemStack s : tmp)
						{
							if(!subStacks.contains(s))
							{
								subStacks.add(s.copy());
							}
						}
					}
				}
			} else if(stack.getBaseStack().getItemDamage() == OreDictionary.WILDCARD_VALUE)
			{
				stack.getBaseStack().getItem().getSubItems(stack.getBaseStack().getItem(), CreativeTabs.tabAllSearch, subStacks);
			}
			
			if(subStacks.size() <= 0)
			{
				subStacks.add(stack.getBaseStack());
			}
		}
		
		public void setDescription(String desc)
		{
			this.desc = desc == null? "" : desc;
		}
		
		@Override
		public void drawBackground(int mx, int my, int px, int py, int width)
		{
			GL11.glPushMatrix();
			
			RenderUtils.DrawLine(px, py, px + width, py, 1F, getTextColor());
			
			GL11.glColor4f(1F, 1F, 1F, 1F);
			
			GL11.glTranslatef(px, py, 0F);
			//GL11.glScalef(2F, 2F, 2F);
			
			this.mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
			this.drawTexturedModalRect(0, 0, 0, 48, 18, 18);
			
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			
			if(stack != null)
			{
				ItemStack tmpStack = subStacks.get((int)(Minecraft.getSystemTime()/1000)%subStacks.size()).copy();
				tmpStack.setTagCompound(stack.GetTagCompound());
				
				try
				{
					RenderUtils.RenderItemStack(mc, tmpStack, 1, 1, (stack.stackSize > 1 ? "" + stack.stackSize : ""));
				} catch(Exception e){}
			}
			
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			
			GL11.glPopMatrix();
			
			RenderUtils.drawSplitString(mc.fontRenderer, desc, px + 20, py + 4, width - 20, getTextColor(), false, 0, 0);
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public void drawForeground(int mx, int my, int px, int py, int width)
		{
			if(stack != null && isWithin(mx, my, px + 1, py + 1, 16, 16))
			{
				ItemStack tmpStack = subStacks.get((int)(Minecraft.getSystemTime()/1000)%subStacks.size()).copy();
				tmpStack.setTagCompound(stack.GetTagCompound());
				
				try
				{
					this.drawTooltip(tmpStack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips), mx, my, mc.fontRenderer);
				} catch(Exception e){}
			}
		}
		
		@Override
		public void onMouseClick(int mx, int my, int px, int py, int click, int index)
		{
			if(stack != null && isWithin(mx, my, px + 1, py + 1, 16, 16)) {
				if(Loader.isModLoaded("NotEnoughItems")) {
					try {
						if(click == 0) {
							codechicken.nei.recipe.GuiCraftingRecipe.openRecipeGui("item", stack.getBaseStack());
						} else if(click == 1) {
							codechicken.nei.recipe.GuiUsageRecipe.openRecipeGui("item", stack.getBaseStack());
						}
					} catch(Exception e){}
				}
			}
		}

		@Override
		public int getHeight()
		{
			return 18;
		}

		@Override
		public boolean canDrawOutsideBox(boolean isForeground)
		{
			return isForeground;
		}
	}
}
