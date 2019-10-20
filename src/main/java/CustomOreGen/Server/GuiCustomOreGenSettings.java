package CustomOreGen.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.mojang.blaze3d.platform.GlStateManager;

import CustomOreGen.Server.ConfigOption.DisplayGroup;
import CustomOreGen.Util.Localization;
import net.java.games.input.Mouse;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.OptionSlider;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiCustomOreGenSettings extends Screen
{
    protected final Screen _parentGui;
    protected int refreshGui = 2;
    protected Button _doneButton = null;
    protected Button _resetButton = null;
    protected GuiCustomOreGenSettings.GuiOptionSlot _optionPanel = null;
    protected GuiGroupPanel _groupPanel = null;
    protected String _toolTip = null;

    public GuiCustomOreGenSettings(Screen parentGui)
    {
    	super(new StringTextComponent("Custom Ore Gen"));
        this._parentGui = parentGui;
    }

    @Override
	public void init()
    {
        super.init();
        super.buttons.clear();
        ConfigOption.DisplayGroup currentGroup = this._groupPanel == null ? null : this._groupPanel.getSelectedGroup();

        if (this.refreshGui >= 2)
        {
            if (this.refreshGui >= 3)
            {
                WorldConfig.loadedOptionOverrides[0] = null;
            }

            WorldConfig visibleGroups = null;

            while (visibleGroups == null)
            {
                try
                {
                    visibleGroups = new WorldConfig();
                }
                catch (Exception var7)
                {
                    if (ServerState.onConfigError(var7))
                    {
                        visibleGroups = null;
                        continue;
                    }

                    visibleGroups = WorldConfig.createEmptyConfig();
                }

                WorldConfig.loadedOptionOverrides[0] = new ArrayList<ConfigOption>();
                for (ConfigOption option : visibleGroups.getConfigOptions()) {
                    if (option.getDisplayState() != null && option.getDisplayState() != ConfigOption.DisplayState.hidden)
                    {
                    	WorldConfig.loadedOptionOverrides[0].add(option);
                    }
                }

                if (currentGroup != null)
                {
                    ConfigOption visibleOptions = visibleGroups.getConfigOption(currentGroup.getName());

                    if (visibleOptions instanceof ConfigOption.DisplayGroup)
                    {
                        currentGroup = (ConfigOption.DisplayGroup)visibleOptions;
                    }
                }
            }
        }

        this.refreshGui = 0;
        Vector<ConfigOption.DisplayGroup> visibleGroups1 = new Vector<DisplayGroup>();
        Vector<ConfigOption> visibleOptions1 = new Vector<ConfigOption>();
        
        nextOption: for (ConfigOption option : WorldConfig.loadedOptionOverrides[0]) {
        	
            ConfigOption.DisplayGroup group;

            if (option instanceof ConfigOption.DisplayGroup)
            {
            	for (group = currentGroup; group != option.getDisplayGroup(); group = group.getDisplayGroup())
            	{
            		if (group == null)
                    {
            			continue nextOption;
                    }
            	}

            	visibleGroups1.add((ConfigOption.DisplayGroup)option);
            }
            else
            {
            	for (group = option.getDisplayGroup(); group != currentGroup; group = group.getDisplayGroup())
            	{
            		if (group == null)
            		{
            			continue nextOption;
                    }
            	}

            	visibleOptions1.add(option);
            }
        }

        this._groupPanel = new GuiGroupPanel(0, 20, super.width, 20, currentGroup, visibleGroups1);
        this._optionPanel = new GuiOptionSlot(Minecraft.getInstance(), visibleGroups1.isEmpty() ? 16 : 40, super.height - 30, 25, visibleOptions1);
        this._optionPanel.registerScrollButtons(1, 2);
        this._doneButton = new Button(super.width / 2 - 155, super.height - 24, 150, 20, "Done");
        this._resetButton = new Button(super.width / 2 + 5, super.height - 24, 150, 20, "Defaults");
        super.buttons.add(this._doneButton);
        super.buttons.add(this._resetButton);
    }

    @Override
    protected void actionPerformed(Button button) throws IOException
    {
        super.actionPerformed(button);

        if (button == this._doneButton)
        {
            super.minecraft.displayGuiScreen(this._parentGui);
        }
        else if (button == this._resetButton)
        {
            this.refreshGui = 3;
        }
        else
        {
            this._optionPanel.actionPerformed(button);
        }
    }

    
    @Override
    public void handleMouseInput() throws IOException {
    	super.handleMouseInput();
    	this._optionPanel.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int buttonID) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, buttonID);
        this._groupPanel.mouseClicked(mouseX, mouseY, buttonID);
        this._optionPanel.handleMouseInput();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick)
    {
        this.renderBackground();
        this._optionPanel.render(mouseX, mouseY, partialTick);
        this._groupPanel.render(mouseX, mouseY, partialTick);
        this.drawCenteredString(super.font, "CustomOreGen Options", super.width / 2, 4, 16777215);
        super.render(mouseX, mouseY, partialTick);

        if (this._toolTip != null)
        {
      			List<String> lines = super.font.listFormattedStringToWidth(this._toolTip, 2 * super.width / 5 - 8);
            int[] lineWidths = new int[lines.size()];
            int tipW = 0;
            int tipH = 8 + super.font.FONT_HEIGHT * lines.size();
            int l = 0;

            for (Iterator<String> tipX = lines.iterator(); tipX.hasNext(); ++l)
            {
                String tipY = tipX.next();
                lineWidths[l] = super.font.getStringWidth(tipY) + 8;

                if (tipW < lineWidths[l])
                {
                    tipW = lineWidths[l];
                }
            }

            int tipX = mouseX;
            int tipY = mouseY;

            if (mouseX > 2 * super.width / 5)
            {
                tipX = mouseX - tipW;
            }

            if (mouseY > super.height / 2)
            {
                tipY = mouseY - tipH;
            }

            this.fillGradient(tipX, tipY, tipX + tipW, tipY + tipH, -15724528, -14671840);
            l = 0;

            for (String line : lines) {
            	super.font.drawString(line, tipX + (tipW - lineWidths[l]) / 2 + 4, tipY + 4 + l * super.font.FONT_HEIGHT, 16777215);
            	++l;
            }

            this._toolTip = null;
        }

        if (this.refreshGui > 0)
        {
            this.init();
        }
    }
     
    public class GuiGroupPanel
    {
        protected int posX;
        protected int posY;
        protected int width;
        protected int height;
        protected int _scrollHInset;
        protected int _scrollOffsetX;
        protected int _scrollOffsetMax;
        protected int mouseX;
        protected int mouseY;
        protected final Vector<IOptionControl> _groupButtons;
        protected GuiButton _groupScrollLButton;
        protected GuiButton _groupScrollRButton;
        protected IOptionControl _currentGroup;
        protected GuiButton _currentButton;

        public GuiGroupPanel(int x, int y, int width, int height, ConfigOption.DisplayGroup selGroup, Vector<ConfigOption.DisplayGroup> groups)
        {
            this.posX = 0;
            this.posY = 0;
            this.width = 0;
            this.height = 0;
            this._scrollHInset = 0;
            this._scrollOffsetX = 0;
            this._scrollOffsetMax = 0;
            this.mouseX = 0;
            this.mouseY = 0;
            this._groupButtons = new Vector<IOptionControl>();
            this._groupScrollLButton = null;
            this._groupScrollRButton = null;
            this._currentGroup = null;
            this._currentButton = null;
            this.posX = x;
            this.posY = y;
            this.width = width;
            this.height = height;
            int scrollWidth = 0;
            int curSelX = 0;

            for (int c = -1; !groups.isEmpty() && c < groups.size(); ++c)
            {
                ConfigOption.DisplayGroup group = c < 0 ? null : groups.get(c);
                String ALL = Localization.maybeLocalize("ALL.displayName", "ALL");
                String text = c < 0 ? "[ " + ALL + " ]" : group.getLocalizedDisplayName();
                int btnWidth = font.getStringWidth(text) + 10;
                GuiGroupButton control = new GuiGroupButton(c + 1, scrollWidth, 0, btnWidth, height, text, group);
                this._groupButtons.add(control);

                if (group == selGroup)
                {
                    curSelX = (width - btnWidth) / 2 - scrollWidth;
                    this._currentGroup = control;
                }

                scrollWidth += btnWidth;
            }

            this._scrollHInset = 4;
            this._scrollOffsetMax = width - 2 * this._scrollHInset - scrollWidth;

            if (this._scrollOffsetMax < 0)
            {
                this._groupScrollLButton = new GuiButton(0, x + 4, y, 20, height, "<");
                this._groupScrollRButton = new GuiButton(0, x + width - 24, y, 20, height, ">");
                this._scrollHInset = 26;
                this._scrollOffsetMax = width - 2 * this._scrollHInset - scrollWidth;
                this._scrollOffsetX = Math.min(Math.max(curSelX - this._scrollHInset, this._scrollOffsetMax), 0);
            }
            else
            {
                this._scrollOffsetX = this._scrollOffsetMax / 2;
            }
        }

        public ConfigOption.DisplayGroup getSelectedGroup()
        {
            return this._currentGroup == null ? null : (ConfigOption.DisplayGroup)this._currentGroup.getOption();
        }

        public int getScrollPos()
        {
            return this._scrollOffsetX;
        }

        public boolean isInScrollArea(int x, int y)
        {
            int rx = x - this.posX;
            int ry = y - this.posY;
            return rx >= this._scrollHInset && rx <= this.width - this._scrollHInset && ry > 0 && ry < this.height;
        }

        public void render(int mouseX, int mouseY, float partialTick)
        {
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            Iterator<IOptionControl> tess = this._groupButtons.iterator();

            while (tess.hasNext())
            {
                IOptionControl texSize = tess.next();
                texSize.getControl().render(mouseX, mouseY, partialTick);
            }

            double texSize1 = 32.0D;
            GlStateManager.disableDepthTest();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexbuffer = tessellator.getBuffer();
            minecraft.getTextureManager().bindTexture(OPTIONS_BACKGROUND);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexbuffer.pos((double)this.posX, (double)(this.posY + this.height), 0.0D).tex(0.0D, (double)this.height / texSize1);
            vertexbuffer.pos((double)(this.posX + this._scrollHInset), (double)(this.posY + this.height), 0.0D).tex((double)this._scrollHInset / texSize1, (double)this.height / texSize1);
            vertexbuffer.pos((double)(this.posX + this._scrollHInset), (double)this.posY, 0.0D).tex((double)this._scrollHInset / texSize1, 0.0D);
            vertexbuffer.pos((double)this.posX, (double)this.posY, 0.0D).tex(0.0D, 0.0D);
            vertexbuffer.pos((double)(this.posX + this.width - this._scrollHInset), (double)(this.posY + this.height), 0.0D).tex(0.0D, (double)this.height / texSize1);
            vertexbuffer.pos((double)(this.posX + this.width), (double)(this.posY + this.height), 0.0D).tex((double)this._scrollHInset / texSize1, (double)this.height / texSize1);
            vertexbuffer.pos((double)(this.posX + this.width), (double)this.posY, 0.0D).tex((double)this._scrollHInset / texSize1, 0.0D);
            vertexbuffer.pos((double)(this.posX + this.width - this._scrollHInset), (double)this.posY, 0.0D).tex(0.0D, 0.0D);
            tessellator.draw();

            if (this._groupScrollLButton != null)
            {
                this._groupScrollLButton.render(mouseX, mouseY, partialTick);

                if (this._groupScrollLButton == this._currentButton)
                {
                    this._scrollOffsetX = Math.min(0, this._scrollOffsetX + 1);
                }
            }

            if (this._groupScrollRButton != null)
            {
                this._groupScrollRButton.render(mouseX, mouseY, partialTick);

                if (this._groupScrollRButton == this._currentButton)
                {
                    this._scrollOffsetX = Math.max(this._scrollOffsetMax, this._scrollOffsetX - 1);
                }
            }

            if (this._currentButton != null && !Mouse.isButtonDown(0))
            {
                this._currentButton = null;
            }
        }

        public void mouseClicked(int mouseX, int mouseY, int buttonID)
        {
            this.mouseX = mouseX;
            this.mouseY = mouseY;

            if (buttonID == 0)
            {
                this._currentButton = null;
                
                for (IOptionControl control : this._groupButtons) {
                    if (control.getControl().mousePressed(minecraft, mouseX, mouseY))
                    {
                        this._currentButton = control.getControl();
                        minecraft.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        break;
                    }
                }

                if (this._groupScrollLButton != null && this._groupScrollLButton.mousePressed(minecraft, mouseX, mouseY))
                {
                    this._currentButton = this._groupScrollLButton;
                }
                else if (this._groupScrollRButton != null && this._groupScrollRButton.mousePressed(minecraft, mouseX, mouseY))
                {
                    this._currentButton = this._groupScrollRButton;
                }
            }
        }
        
        class GuiGroupButton extends GuiButton implements IOptionControl
        {
            protected final ConfigOption.DisplayGroup _group;
            private final int _relX;
            private final int _relY;

            public GuiGroupButton(int id, int relX, int relY, int width, int height, String text, ConfigOption.DisplayGroup group)
            {
                super(id, relX, relY, width, height, text);
                this._group = group;
                this._relX = relX;
                this._relY = relY;
            }

            public ConfigOption getOption()
            {
                return this._group;
            }

            public GuiButton getControl()
            {
                return this;
            }

            private boolean isButtonVisible()
            {
                super.x = posX + _scrollHInset + _scrollOffsetX + this._relX;
                super.y = posY + this._relY;
                return !isInScrollArea(super.x, super.y) && !isInScrollArea(super.x + super.width, super.y + super.height);
            }

            @Override
            public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY)
            {
                if (this.isButtonVisible() && this.isMouseOver() && super.mousePressed(minecraft, mouseX, mouseY))
                {
                    _currentGroup = this;
                    refreshGui = 1;
                    return true;
                }
                else
                {
                    return false;
                }
            }

            @Override
            public int getHoverState(boolean mouseOver)
            {
                if (isInScrollArea(mouseX, mouseY))
                {
                    return super.getHoverState(mouseOver);
                }
                else
                {
                    super.hovered = false;
                    return 1;
                }
            }

            @Override
            public void render(int mouseX, int mouseY, float partialTick)
            {
                if (this.isButtonVisible() && super.visible)
                {
                    super.hovered = mouseX >= super.x && mouseY >= super.y && mouseX < super.x + super.width && mouseY < super.y + super.height;
                    this.mouseDragged(minecraft, mouseX, mouseY);

                    if (_currentGroup == this)
                    {
                        drawRect(super.x, super.y, super.x + super.width, super.y + super.height, -1610612736);
                    }
                    else
                    {
                        if (this._group == null)
                        {
                            drawRect(super.x, super.y, super.x + 1, super.y + super.height, -16777216);
                        }

                        drawRect(super.x, super.y, super.x + super.width, super.y + 1, -16777216);
                        drawRect(super.x + super.width - 1, super.y, super.x + super.width, super.y + super.height, -16777216);
                    }

                    font fontObj = minecraft.font;
                    int textColor = super.enabled ? (super.hovered ? 16777120 : 14737632) : 10526880;
                    this.drawCenteredString(fontObj, super.displayString, super.x + super.width / 2, super.y + (super.height - 8) / 2, textColor);

                    if (this.isMouseOver() && this._group != null)
                    {
                        _toolTip = this._group.getLocalizedDescription();
                    }
                }
            }
        }

    }
    
    public class GuiOptionSlot extends GuiSlot
    {
        protected final Vector<IOptionControl> _optionControls;
        protected IOptionControl _clickTarget;

        public GuiOptionSlot(Minecraft minecraft, int top, int bottom, int slotHeight, Vector<ConfigOption> options)
        {
            super(minecraft, GuiCustomOreGenSettings.this.width, GuiCustomOreGenSettings.this.height, top, bottom, slotHeight);
            this._optionControls = new Vector<IOptionControl>();
            this._clickTarget = null;

            for (int c = 0; c < options.size(); ++c)
            {
                ConfigOption option = options.get(c);
                
                if (option instanceof ChoiceOption)
                {
                    this._optionControls.add(new GuiChoiceButton(c, 2 * width / 5 + 15, 0, width / 10 + 100, slotHeight - 6, (ChoiceOption)option));
                }
                else if (option instanceof NumericOption)
                {
                    this._optionControls.add(new GuiNumericSlider(c, 2 * width / 5 + 15, 0, width / 10 + 100, slotHeight - 6, (NumericOption)option));
                }
            }
        }

        protected int getSize()
        {
            return this._optionControls.size();
        }

        @Override
        protected void elementClicked(int index, boolean doubleClicked, int mouseX, int mouseY)
        {
            if (this._clickTarget != null)
            {
                this._clickTarget.getControl().mouseReleased(mouseX, mouseY);

                if (this._clickTarget.getOption().getDisplayState() == ConfigOption.DisplayState.shown_dynamic)
                {
                    refreshGui = 2;
                }

                this._clickTarget = null;
            }

            IOptionControl control = (IOptionControl)this._optionControls.get(index);

            if (control.getControl().mousePressed(minecraft, mouseX, mouseY))
            {
            	minecraft.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this._clickTarget = control;
            }
        }

        protected boolean isSelected(int index)
        {
            return false;
        }

        public boolean isInBounds(int x, int y)
        {
            return super.mouseY >= super.top && super.mouseY <= super.bottom && super.mouseX >= 0 && super.mouseX <= width;
        }

        protected void drawBackground() {}
        
        @Override
        protected void drawSlot(int index, int slotX, int slotY, int slotH, int mouseX, int mouseY, float partialTick)
        {
            IOptionControl optCtrl = (IOptionControl)this._optionControls.get(index);
            ConfigOption option = optCtrl.getOption();
            GuiButton control = optCtrl.getControl();
            String optionName = option.getLocalizedDisplayName();
            int nameW = font.getStringWidth(optionName);
            int nameX = 2 * width / 5 - 15 - nameW;
            int nameH = font.FONT_HEIGHT;
            int nameY = slotY + 8;
            drawString(font, optionName, nameX, nameY, 16777215);

            if (super.mouseX >= nameX && super.mouseX <= nameX + nameW && super.mouseY >= nameY && super.mouseY <= nameY + nameH && this.isInBounds(super.mouseX, super.mouseY))
            {
                _toolTip = option.getLocalizedDescription();
            }

            control.y = slotY + 3;
            control.render(super.mouseX, super.mouseY, partialTick);
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTick)
        {
            super.render(mouseX, mouseY, partialTick);

            if (this._clickTarget != null && !Mouse.isButtonDown(0))
            {
                this._clickTarget.getControl().mouseReleased(mouseX, mouseY);

                if (this._clickTarget.getOption().getDisplayState() == ConfigOption.DisplayState.shown_dynamic)
                {
                    refreshGui = 2;
                }

                this._clickTarget = null;
            }
        }
        
        class GuiChoiceButton extends Button implements IOptionControl
        {
            private final ChoiceOption _choice;
            private final int _maxWidth;
            private int mouseX;
            private int mouseY;

            public GuiChoiceButton(int id, int x, int y, int maxWidth, int height, ChoiceOption choice)
            {
                super(id, x, y, maxWidth, height, (String)null);
                this.mouseX = 0;
                this.mouseY = 0;
                this._choice = choice;
                this._maxWidth = maxWidth;
                this.onValueChanged();
            }

            public ConfigOption getOption()
            {
                return this._choice;
            }

            public Button getControl()
            {
                return this;
            }

            protected void onValueChanged()
            {
                super.displayString = this._choice.getLocalizedDisplayValue();
                int strWidth = super.displayString == null ? 0 : font.getStringWidth(super.displayString);
                super.width = Math.min(this._maxWidth, strWidth + 10);
            }

            @Override
            public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY)
            {
                if (super.mousePressed(minecraft, mouseX, mouseY))
                {
                    this._choice.setValue(this._choice.nextPossibleValue());
                    this.onValueChanged();
                    return true;
                }
                else
                {
                    return false;
                }
            }

            @Override
            public int getHoverState(boolean mouseOver)
            {
                if (GuiOptionSlot.this.isInBounds(this.mouseX, this.mouseY))
                {
                    return super.getHoverState(mouseOver);
                }
                else
                {
                    super.hovered = false;
                    return 1;
                }
            }

            @Override
            public void render(int mouseX, int mouseY, float partialTick)
            {
                super.render(mouseX, mouseY, partialTick);

                if (this.isMouseOver())
                {
                    _toolTip = this._choice.getLocalizedValueDescription();
                }
            }
        }

        class GuiNumericSlider extends OptionSlider implements IOptionControl
        {
            private final NumericOption _numeric;
            
            public GuiNumericSlider(int id, int x, int y, int width, int height, NumericOption numeric)
            {
                super(id, x, y, GameSettings.Options.ANAGLYPH);            
                this._numeric = numeric;
                super.width = width;
                super.height = height;
                ReflectionHelper.setPrivateValue(GuiOptionSlider.class, this, (float)numeric.getNormalizedDisplayValue(), 0);
                this.onValueChanged();
            }
            
			public ConfigOption getOption()
            {
                return this._numeric;
            }

            public Button getControl()
            {
                return this;
            }

            protected void onValueChanged()
            {
                long prec = 6L;
                long base = (long)Math.pow(10.0D, (double)prec);

                for (long val = Math.round(this._numeric.getDisplayIncr() * (double)base); prec > 0L && val % 10L == 0L; --prec)
                {
                    val /= 10L;
                    base /= 10L;
                }

                super.displayString = String.format("%." + prec + "f", new Object[] {Double.valueOf(this._numeric.getDisplayValue())});
            }
            
            private void updateSliderValue(int mouseX) {
            	double sliderValue = (float)(mouseX - (this.x + 4)) / (float)(this.width - 8);

                if (sliderValue < 0.0F)
                {
                    sliderValue = 0.0F;
                }

                if (sliderValue > 1.0F)
                {
                    sliderValue = 1.0F;
                }
                
                this._numeric.setNormalizedDisplayValue(sliderValue);
            	this.onValueChanged();
            }

            @Override
            public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY)
            {
                if (super.mousePressed(minecraft, mouseX, mouseY))
                {
                	this.updateSliderValue(mouseX);
                    return true;
                }
                else
                {
                    return false;
                }
            }

            @Override
            protected void mouseDragged(Minecraft minecraft, int mouseX, int mouseY)
            {
                super.mouseDragged(minecraft, mouseX, mouseY);
                if (super.dragging) {
                	this.updateSliderValue(mouseX);
                }
            }
        }
    }

    public interface IOptionControl
    {
        ConfigOption getOption();

        Button getControl();
    }

}
