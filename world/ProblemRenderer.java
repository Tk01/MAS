package world;

import java.util.Set;

import javax.annotation.Nullable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;

import WorldModel.Goal;
import WorldModel.Robot;

import com.github.rinde.rinsim.core.model.ModelProvider;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.renderers.ModelRenderer;
import com.github.rinde.rinsim.ui.renderers.ViewPort;
import com.github.rinde.rinsim.ui.renderers.ViewRect;
import com.google.common.base.Optional;

public class ProblemRenderer implements ModelRenderer{
	

		  Optional<RoadModel> rm;
		  Optional<DefaultPDPModel> pm;


		  ProblemRenderer() {
		    rm = Optional.absent();
		    pm = Optional.absent();
		  }

		  @Override
		  public void registerModelProvider(ModelProvider mp) {
		    rm = Optional.fromNullable(mp.tryGetModel(RoadModel.class));
		    pm = Optional.fromNullable(mp.tryGetModel(DefaultPDPModel.class));
		  }

		  @Override
		  public void renderStatic(GC gc, ViewPort vp) {}

		  @Override
		  public void renderDynamic(GC gc, ViewPort vp, long time) {
		    final Set<Robot> taxis = rm.get().getObjectsOfType(Robot.class);
		    synchronized (taxis) {
		      for (final Robot t : taxis) {
		        final Point p = rm.get().getPosition(t);
		        final int x = vp.toCoordX(p.x) - 5;
		        final int y = vp.toCoordY(p.y) - 30;

		        final Goal vs = t.getGoal();
		        String text= "";
		        if(vs !=null) text+=	vs.toString()+";";
		         text = text +"b=" + t.getBattery();
		        if (text != null) {
		          final org.eclipse.swt.graphics.Point extent = gc.textExtent(text);

		          gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_BLUE));
		          gc.fillRoundRectangle(x - (extent.x / 2), y - (extent.y / 2),
		              extent.x + 2, extent.y + 2, 5, 5);
		          gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));

		          gc.drawText(text, x - (extent.x / 2) + 1, y - (extent.y / 2) + 1,
		              true);
		        }
		      }
		    }
		  }

		  @Nullable
		  @Override
		  public ViewRect getViewRect() {
		    return null;
		  }
}
