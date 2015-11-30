package amidst.map.layer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import amidst.map.Fragment;
import amidst.map.Map;
import amidst.minecraft.world.CoordinatesInWorld;
import amidst.minecraft.world.Resolution;
import amidst.minecraft.world.World;

public abstract class ImageLayer extends Layer {
	private final AffineTransform imageLayerMatrix = new AffineTransform();

	protected final Resolution resolution;
	private final int size;
	private final int[] rgbArray;
	private BufferedImage bufferedImage;

	public ImageLayer(World world, Map map, LayerType layerType,
			Resolution resolution) {
		super(world, map, layerType);
		this.resolution = resolution;
		this.size = resolution.getStepsPerFragment();
		this.rgbArray = new int[size * size];
		this.bufferedImage = createBufferedImage();
	}

	private BufferedImage createBufferedImage() {
		return new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
	}

	@Override
	public void construct(Fragment fragment) {
		fragment.putImage(layerType, createBufferedImage());
	}

	@Override
	public void load(Fragment fragment) {
		doLoad(fragment);
	}

	@Override
	public void reload(Fragment fragment) {
		doLoad(fragment);
	}

	protected void doLoad(Fragment fragment) {
		CoordinatesInWorld corner = fragment.getCorner();
		long cornerX = corner.getXAs(resolution);
		long cornerY = corner.getYAs(resolution);
		drawToCache(fragment, cornerX, cornerY);
		bufferedImage.setRGB(0, 0, size, size, rgbArray, 0, size);
		bufferedImage = fragment.getAndSetImage(layerType, bufferedImage);
	}

	protected void drawToCache(Fragment fragment, long cornerX, long cornerY) {
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				int index = getCacheIndex(x, y);
				rgbArray[index] = getColorAt(fragment, cornerX, cornerY, x, y);
			}
		}
	}

	private int getCacheIndex(int x, int y) {
		return x + y * size;
	}

	@Override
	public void draw(Fragment fragment, Graphics2D g2d,
			AffineTransform layerMatrix) {
		initImageLayerMatrix(resolution.getStep(), layerMatrix);
		g2d.setTransform(imageLayerMatrix);
		if (g2d.getTransform().getScaleX() < 1.0f) {
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		} else {
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		}
		g2d.drawImage(fragment.getImage(layerType), 0, 0, null);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	}

	// TODO: is this transformation correct?
	public void initImageLayerMatrix(double scale, AffineTransform layerMatrix) {
		imageLayerMatrix.setTransform(layerMatrix);
		imageLayerMatrix.scale(scale, scale);
	}

	protected abstract int getColorAt(Fragment fragment, long cornerX,
			long cornerY, int x, int y);
}
