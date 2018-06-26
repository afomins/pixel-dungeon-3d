/*
 * Pixel Dungeon 3D
 * Copyright (C) 2016-2018 Alex Fomins
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

//------------------------------------------------------------------------------
package com.matalok.pd3d;

//-----------------------------------------------------------------------------
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd.PdLocal;
import com.matalok.pd3d.Tweener.FloatAccessor;
import com.matalok.pd3d.Tweener.Vector3Accessor;
import com.matalok.pd3d.engine.Engine;
import com.matalok.pd3d.engine.gui.EngineWndGame;
import com.matalok.pd3d.gui.Gui;
import com.matalok.pd3d.level.Level;
import com.matalok.pd3d.level.LevelCamera;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.node.GameNodeTweener;
import com.matalok.pd3d.proxy.LocalQueue;
import com.matalok.pd3d.renderable.RenderableMan;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.renderer.layer.RendererLayer;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;
import com.matalok.pd3d.shared.UtilsClass;
import com.matalok.pd3d.shared.UtilsClass.Callback;
import com.matalok.scenegraph.SgMan;
import com.matalok.scenegraph.SgNode;
import com.matalok.scenegraph.SgObject;
import com.matalok.scenegraph.SgWriterLog;

//------------------------------------------------------------------------------
public class Main 
  extends GameNode
  implements ApplicationListener, Scheduler.IClient {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    public static Main inst;

    // *************************************************************************
    // Main
    // *************************************************************************
    public Config cfg;
    public SgMan sg_man;

    public GeomBuilder geom_builder;
    public Timer timer;
    public InputMan input_man;
    public Scheduler scheduler;
    public TextRenderer text_renderer;
    public Gui gui;
    public Renderer renderer;
    public ProxyClient proxy_client;
    public Engine engine;
    public Level level;
    public RenderableMan renderable_man;
    public LevelCamera level_camera;
    public PdLocal pd_local;
    public SnapshotMan snapshot;
    public SaveGameMan save_game;

    private UtilsClass.PeriodicTask m_profiler_log_task;

    //--------------------------------------------------------------------------
    public Main(PlatformAPI platform_api, Config cfg) {
        super("main", 1.0f);

        // Register self as singleton
        inst = this;
        inst.cfg = cfg;

        // Platform API
        PlatformUtils.api = platform_api;
    }

    //--------------------------------------------------------------------------
    public void SaveConfig() {
        Config.Save(cfg);
    }

    //--------------------------------------------------------------------------
    public void FakeResize() {
        resize(renderer.GetWidth(), renderer.GetHeight());
    }

    //--------------------------------------------------------------------------
    public void EnableProfiler() {
        if(!cfg.prof_enable) {
            return;
        }

        // Enable profiler hierarchy
        sg_man.EnablePerformanceCounter(this, GameNode.RENDER);
        sg_man.EnablePerformanceCounter(this, GameNode.POST_RENDER);
        sg_man.EnablePerformanceCounter(null, null);
        sg_man.EnablePerformanceCounter(pd_local, GameNode.RENDER);
        sg_man.EnablePerformanceCounter(proxy_client, GameNode.RENDER);
        sg_man.EnablePerformanceCounter(level, GameNode.RENDER);
        sg_man.EnablePerformanceCounter(null, null);
        sg_man.EnablePerformanceCounter(level, GameNode.POST_RENDER);
        sg_man.EnablePerformanceCounter(engine, GameNode.POST_RENDER);
        sg_man.EnablePerformanceCounter(gui, GameNode.POST_RENDER);
        sg_man.EnablePerformanceCounter(renderer, GameNode.POST_RENDER);
        sg_man.EnablePerformanceCounter(null, null);
        for(RendererLayer r : renderer.GetRenderables()) {
            sg_man.EnablePerformanceCounter(r, GameNode.POST_RENDER);
        }

        // Create profiler task
        m_profiler_log_task = new UtilsClass.PeriodicTask(
          Main.inst.timer.GetCur(),
          Utils.SecToMsec(Main.inst.cfg.prof_log_interval), 
            new UtilsClass.Callback() {
                //--------------------------------------------------------------
                @Override public Object Run(Object... args) {
                    Logger.d("--- [GL profiler] --------------------------");
                    for(RendererLayer r : render_ctx.layers) {
                        r.stat_profiler.Log();
                    }
                    renderer.GetProfilerStats().Log();

                    Logger.d("--- [Performance counters] -----------------");
                    sg_man.LogPerformanceCounters();

                    Logger.d("--- [Misc] ---------------------------------");

                    Runtime rt = Runtime.getRuntime();
                    float mem_total = (float)rt.totalMemory() / (1024 * 1024);
                    float mem_free = (float)rt.freeMemory() / (1024 * 1024);
                    int model_all_num = renderer.GetModelNum(false);
                    int model_visible_num = renderer.GetModelNum(true);
                    int trig_all_num = renderer.GetTriangleNum(false);
                    int trig_visible_num = renderer.GetTriangleNum(true);
                    Logger.d(" >> name=%s fps=%d mem-total=%.1fmb mem-free=%.1fmb mem-usage=%.1fmb", 
                      Utils.GetPaddedString("misc", cfg.prof_perf_cnt_log_pad), 
                      renderer.GetFps(), mem_total, mem_free, mem_total - mem_free);
                    Logger.d(" >> name=%s total-num=%d visible-num=%d(%d%%)", 
                      Utils.GetPaddedString("model", cfg.prof_perf_cnt_log_pad), 
                      model_all_num, model_visible_num, 
                      (int)(100.0f * model_visible_num / model_all_num));
                    Logger.d(" >> name=%s total-num=%d visible-num=%d(%d%%)", 
                      Utils.GetPaddedString("triangle", cfg.prof_perf_cnt_log_pad), 
                      trig_all_num, trig_visible_num, 
                      (int)(100.0f * trig_visible_num / trig_all_num));
                    Logger.d("--------------------------------------------");
                    return null;
             }});
    }

    //--------------------------------------------------------------------------
    public void BeginProfiler(double fuckoff) {
        if(!cfg.prof_enable) {
            return;
        }
        sg_man.UpdatePerformanceCounters();
    }

    //--------------------------------------------------------------------------
    public void EndProfiler() {
        if(!cfg.prof_enable) {
            return;
        }
        m_profiler_log_task.Run(timer.GetCur());
    }

    // *************************************************************************
    // ApplicationListener
    // *************************************************************************
    @Override public void create() {
        //......................................................................
        //
        // Logger
        //
        Logger.Register(
            new Logger() {
                private FileHandle m_file;

                @Override public synchronized void WriteRaw(String str) {
                    // Write to console
                    System.out.println(str);

                    // Write to file
                    if(m_file != null) {
                        m_file.writeString(str, true);
                    }
                }

                @Override public Logger Init() {
//                    m_file = PlatformUtils.OpenLocalFile("game.log", true);
                    return this;
                }
            },

            new Callback() {
                @Override public Object Run(Object... args) {
                    EngineWndGame game_wnd = 
                      (engine == null) ? null : engine.GetGameWindow(false);
                    if(game_wnd == null || args == null || args.length != 1 || 
                      !(args[0] instanceof String)) {
                        return null;
                    }
                    game_wnd.WriteLog("-- " + (String)args[0]);
                    return null;
                }
        });
        Logger.SetLogging(Main.inst.cfg.app_logging_enable);
        Logger.SetPrefix("C");

        //......................................................................
        //
        // Platform
        //
        SgObject.SetPlatform(this);
        LocalQueue.Reset();

        //......................................................................
        //
        // Override default config
        //
        boolean is_android = PlatformUtils.api.GetPlatformName().equals("android");
        if(Config.default_created && is_android) {
            cfg.app_landscape = false;

            int w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
            int font_size_threshold = 480;
            if(w >= font_size_threshold || h >= font_size_threshold) {
                cfg.gui_font_scale = 2;
            }
            Config.Save(cfg);
        }

        //......................................................................
        //
        // Tweener
        //
        Tweener.RegisterClass(UtilsClass.FFloat.class, new FloatAccessor());
        Tweener.RegisterClass(Vector3.class, new Vector3Accessor());
        Tweener.RegisterClass(GameNode.class, new GameNodeTweener());

        //......................................................................
        //
        // Scene-graph
        //
        sg_man = new SgMan(this);
        timer = (Timer)SgAddChild(new Timer());
        input_man = (InputMan)SgAddChild(new InputMan());
        scheduler = (Scheduler)SgAddChild(new Scheduler());
        geom_builder = (GeomBuilder)SgAddChild(new GeomBuilder());
        proxy_client = (ProxyClient)SgAddChild(new ProxyClient());
        renderer = (Renderer)SgAddChild(new Renderer());
        text_renderer = (TextRenderer)SgAddChild(new TextRenderer());
        gui = (Gui)SgAddChild(new Gui());
        renderable_man = (RenderableMan)SgAddChild(new RenderableMan());
        engine = (Engine)SgAddChild(new Engine());
        level = (Level)SgAddChild(new Level());
        level_camera = (LevelCamera)SgAddChild(new LevelCamera());
        pd_local = (PdLocal)SgAddChild(new PdLocal());
        snapshot = (SnapshotMan)SgAddChild(new SnapshotMan());
        save_game = (SaveGameMan)SgAddChild(new SaveGameMan());

        //......................................................................
        //
        // Finalize
        //

        // Refresh list of savegames
        save_game.Refresh();

        // Register to all scheduler events 
        scheduler.RegisterClient(this);

        // Start engine
        scheduler.ScheduleEvent(
          Scheduler.Event.SWITCH_ENGINE_SCENE, "scene-connect");

        // Enable profiler
        EnableProfiler();

        // Adjust screen orientation according to config
        boolean is_landscape = renderer.IsLandscape();
        if(cfg.app_landscape && !is_landscape) {
            renderer.SetLandscapeScreen();
        } else if(!cfg.app_landscape && is_landscape){
            renderer.SetPortraitScreen();
        }

        // Force immersive mode on android
        if(is_android) {
            PlatformUtils.api.SetFullscreen(true);
        }
    }

    //--------------------------------------------------------------------------
    // RESIZE
    private GameNode.ResizeCtx resize_ctx = new GameNode.ResizeCtx();
    @Override public void resize(int width, int height) {
        resize_ctx.Init(width, height);
        SgNode.Walk(this, true, GameNode.RESIZE.ResetStats(), 0, resize_ctx);
    }

    //--------------------------------------------------------------------------
    // RENDER
    private GameNode.RenderCtx render_ctx = new GameNode.RenderCtx();
    @Override public void render() {
        //......................................................................
        // Artificial slowdown
        double fuckoff = 0.0f;
        for(long i = 0; i < Main.inst.cfg.dbg_slowdown; i++) {
            fuckoff += Math.sqrt(i); 
        }

        //......................................................................
        // Begin profiler
        BeginProfiler(fuckoff);

        //......................................................................
        // Render
        Logger.NewStep(true);
        SgNode.Walk(this, true, GameNode.RENDER.ResetStats(), 0, render_ctx);

        // Post-render
        Logger.NewStep(false);
        SgNode.Walk(this, true, GameNode.POST_RENDER.ResetStats(), 0, render_ctx);

        // Cleanup deleted nodes
        Logger.NewStep(false);
        sg_man.RunCleanup();

        //......................................................................
        // End profiler
        EndProfiler();
    }

    //--------------------------------------------------------------------------
    // CLEANUP
    @Override public void dispose() {
        Logger.NewStep(true);
        SgNode.Walk(this, false, GameNode.CLEANUP.ResetStats(), 0, null);
    }

    //--------------------------------------------------------------------------
    // PAUSE
    @Override public void pause() { 
        Logger.NewStep(true);
        SgNode.Walk(this, false, GameNode.PAUSE.ResetStats(), 0, null);
    }

    //--------------------------------------------------------------------------
    // RESUME
    @Override public void resume() {
        Logger.NewStep(true);
        SgNode.Walk(this, true, GameNode.RESUME.ResetStats(), 0, null);
    }

    // *************************************************************************
    // METHODS
    // *************************************************************************
    @Override public boolean OnMethodCleanup() {
        super.OnMethodCleanup();

        inst = null;
        return true;
    }

    // *************************************************************************
    // ISchedulerClient
    // *************************************************************************
    @Override public void OnEvent(Scheduler.Event event, String arg) {
        switch(event) {
        //......................................................................
        case QUIT: {
            Gdx.app.exit();
        } break;

        //......................................................................
        case LOG_SCENE_GRAPH: {
            sg_man.RunJson(this, new SgWriterLog(this), " ", 0, 
              SgNode.JsonTarget.COMMON, SgNode.JsonTarget.SNAPSHOT);
        } break;

        //......................................................................
        case SAVE_SNAPSHOT: {
            snapshot.SaveSnapshot();
        } break;

        //......................................................................
        case LOAD_NEXT_SNAPSHOT: {
            snapshot.LoadNextSnapshot();
        } break;

        //......................................................................
        default:
        }
    }

    //**************************************************************************
    // SgUtils.IPlatform
    //**************************************************************************
    @Override public void Dbg(String fmt, Object... args) {
        Logger.d(fmt, args);
    }

    //--------------------------------------------------------------------------
    @Override public void Inf(String fmt, Object... args) {
        Logger.i(fmt, args);
    }

    //--------------------------------------------------------------------------
    @Override public void Err(String fmt, Object... args) {
        Logger.e(fmt, args);
    }

    //--------------------------------------------------------------------------
    @Override public void Assert(boolean statement, String fmt, Object... args) {
        Utils.Assert(statement, fmt, args);
    }
}
