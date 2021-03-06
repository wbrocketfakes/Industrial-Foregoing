/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2019, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.buuz135.industrial.proxy.client.render;

import com.buuz135.industrial.block.transport.ConveyorBlock;
import com.buuz135.industrial.block.transport.tile.ConveyorTile;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class FluidConveyorTESR extends TileEntityRenderer<ConveyorTile> {
    @Override
    public void render(ConveyorTile te, double x, double y, double z, float partialTicks, int destroyStage) {
        super.render(te, x, y, z, partialTicks, destroyStage);
        if (te.getTank().getFluidAmount() > 0) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float) x, (float) y, (float) z);
            Direction facing = te.getFacing();
            if (facing == Direction.NORTH) {
                GlStateManager.translatef(1, 0, 1);
                GlStateManager.rotatef(180, 0, 1, 0);
            }
            if (facing == Direction.EAST) {
                GlStateManager.translatef(0, 0, 1);
                GlStateManager.rotatef(90, 0, 1, 0);
            }
            if (facing == Direction.WEST) {
                GlStateManager.translatef(1, 0, 0);
                GlStateManager.rotatef(-90, 0, 1, 0);
            }
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableBlend();
            GlStateManager.disableCull();

            if (Minecraft.isAmbientOcclusionEnabled()) {
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
            } else {
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            FluidStack fluid = te.getTank().getFluid();
            TextureAtlasSprite flow = Minecraft.getInstance().getTextureMap().getAtlasSprite(fluid.getFluid().getAttributes().getFlowing(fluid).toString());
            TextureAtlasSprite still = Minecraft.getInstance().getTextureMap().getAtlasSprite(fluid.getFluid().getAttributes().getStill(fluid).toString());
            double posY = 2 / 16f - 1 / 32f;
            double right = 1 / 16f;
            double left = 15 / 16f;
            ConveyorBlock.EnumSides sides = te.getWorld().getBlockState(te.getPos()).getBlock().getExtendedState(te.getWorld().getBlockState(te.getPos()), te.getWorld(), te.getPos()).get(ConveyorBlock.SIDES);
            if (sides == ConveyorBlock.EnumSides.BOTH || sides == ConveyorBlock.EnumSides.RIGHT) right = 0;
            if (sides == ConveyorBlock.EnumSides.BOTH || sides == ConveyorBlock.EnumSides.LEFT) left = 1;
            Color color = new Color(fluid.getFluid().getAttributes().getColor(te.getTank().getFluid()));

            buffer.pos(right, posY, 0).tex(flow.getInterpolatedU(0), flow.getInterpolatedV(0)).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            buffer.pos(left, posY, 0).tex(flow.getInterpolatedU(8), flow.getInterpolatedV(0)).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            buffer.pos(left, posY, 1).tex(flow.getInterpolatedU(8), flow.getInterpolatedV(8)).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            buffer.pos(right, posY, 1).tex(flow.getInterpolatedU(0), flow.getInterpolatedV(8)).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

            boolean shouldRenderNext = !(te.getWorld().getTileEntity(te.getPos().offset(facing)) instanceof ConveyorTile) || ((ConveyorTile) te.getWorld().getTileEntity(te.getPos().offset(facing))).getTank().getFluidAmount() <= 0;
            if (shouldRenderNext) {
                buffer.pos(right, posY, 1).tex(still.getInterpolatedU(0), still.getInterpolatedV(0)).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                buffer.pos(left, posY, 1).tex(still.getInterpolatedU(8), still.getInterpolatedV(0)).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                buffer.pos(left, 1 / 16D, 1).tex(still.getInterpolatedU(8), still.getInterpolatedV(8)).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                buffer.pos(right, 1 / 16D, 1).tex(still.getInterpolatedU(0), still.getInterpolatedV(8)).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            }
            boolean shouldRenderPrev = !(te.getWorld().getTileEntity(te.getPos().offset(facing.getOpposite())) instanceof ConveyorTile) || ((ConveyorTile) te.getWorld().getTileEntity(te.getPos().offset(facing.getOpposite()))).getTank().getFluidAmount() <= 0;
            if (shouldRenderPrev) {
                buffer.pos(right, posY, 0).tex(still.getInterpolatedU(0), still.getInterpolatedV(0)).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                buffer.pos(left, posY, 0).tex(still.getInterpolatedU(8), still.getInterpolatedV(0)).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                buffer.pos(left, 1 / 16D, 0).tex(still.getInterpolatedU(8), still.getInterpolatedV(8)).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                buffer.pos(right, 1 / 16D, 0).tex(still.getInterpolatedU(0), still.getInterpolatedV(8)).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            }

            tessellator.draw();
            GlStateManager.popMatrix();
        }
    }
}
