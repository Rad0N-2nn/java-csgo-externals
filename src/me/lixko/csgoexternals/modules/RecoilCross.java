package me.lixko.csgoexternals.modules;

import com.github.jonatino.misc.MemoryBuffer;

import me.lixko.csgoexternals.Client;
import me.lixko.csgoexternals.Engine;
import me.lixko.csgoexternals.offsets.Offsets;
import me.lixko.csgoexternals.structs.Vector;
import me.lixko.csgoexternals.util.DrawUtils;

public class RecoilCross extends Module {
	
	boolean needsDataUpdate = false;
	Vector punchvec = new Vector();
	MemoryBuffer lpvecbuf = new MemoryBuffer(punchvec.size());
	
	private final int fov = 90;
    private final int sx = (int) (DrawUtils.getWidth() * 0.5f);
    private final int sy = (int) (DrawUtils.getHeight() * 0.5f);
    private final int dx = DrawUtils.getWidth() / fov;
    private final int dy = DrawUtils.getHeight() / fov;
    private int crosshairX = sx;
    private int crosshairY = sy;
    private float crossalpha = 0.0f;
	
	Thread updateLoop = new Thread(new Runnable() {
		@Override
		public void run() {
			while (Client.theClient.isRunning) {
				try {
					Thread.sleep(1);
					if(!needsDataUpdate) continue;

					Engine.clientModule().read(Offsets.m_dwLocalPlayer + 0x36f0 + 0x74, lpvecbuf.size(), lpvecbuf);
					float pvecy = punchvec.y.getFloat();
					float pvecx = punchvec.x.getFloat();
					crosshairX = (int) (sx - (dx * pvecy));
				    crosshairY = (int) (sy - (dy * pvecx));
				    crossalpha = (float) Math.min(((Math.abs(pvecx) + Math.abs(pvecy)) * 0.7f), 1.0f);
					needsDataUpdate = false;
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}
	});
	
	@Override
	public void onUIRender() {
		if(!Client.theClient.isRunning || this.needsDataUpdate) return;
		this.needsDataUpdate = true;
	    
	    DrawUtils.setColor(0, 0, 0, Math.max(crossalpha - 0.2f, 0.0f));
	    DrawUtils.fillRectanglew(crosshairX-6, crosshairY-1, 12, 3);
	    DrawUtils.fillRectanglew(crosshairX-1, crosshairY-6, 3, 12);
	    
	    DrawUtils.setColor(1.0f, 1.0f, 1.0f, crossalpha);
	    DrawUtils.drawLine(crosshairX - 5, crosshairY+1, crosshairX + 5, crosshairY+1);
	    DrawUtils.drawLine(crosshairX+1, crosshairY + 5, crosshairX+1, crosshairY - 5);
	    
	    /*DrawUtils.setColor(0, 0, 0, 150);
	    DrawUtils.fillRectanglew(sx-2, sy-2, 4, 4);
	    DrawUtils.setColor(0x00FFFFFF);
	    DrawUtils.fillRectanglew(sx-1, sy-1, 2, 2);*/
	}
	
	@Override
	public void onEngineLoaded() {
		punchvec.setSource(lpvecbuf);
		updateLoop.start();		
	}

}
