package com.cyzapps.adapter;

import com.cyzapps.SmartMath.R;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.Jfcalc.FuncEvaluator.FunctionInterrupter;
import com.cyzapps.Jmfp.ScriptAnalyzer;
import com.cyzapps.Jmfp.ScriptAnalyzer.ScriptInterrupter;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.AbstractExpr.AbstractExprInterrupter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StorageActionReceiver extends BroadcastReceiver {
    public class StorageActionFunctionInterrupter extends FunctionInterrupter	{

		@Override
		public boolean shouldInterrupt() {
			return true;	// always interrupt
		}

		@Override
		public void interrupt() throws InterruptedException {
			throw new InterruptedException();
		}
    	
    }
	
    public class StorageActionScriptInterrupter extends ScriptInterrupter	{

		@Override
		public boolean shouldInterrupt() {
			return true;	// always interrupt
		}

		@Override
		public void interrupt() throws InterruptedException {
			throw new InterruptedException();
		}
    	
    }
	
    public class StorageActionAbstractExprInterrupter extends AbstractExprInterrupter	{

		@Override
		public boolean shouldInterrupt() {
			return true;	// always interrupt
		}

		@Override
		public void interrupt() throws InterruptedException {
			throw new InterruptedException();
		}
    	
    }

	@Override
    public void onReceive(Context context, Intent intent) {
		Log.e("StorageAction", intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)){
            onMemcardMounted(context);
        }
        else if (intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)
        		|| intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)
        		|| intent.getAction().equals(Intent.ACTION_MEDIA_BAD_REMOVAL)){
            onMemorycardUnMounted(context);
        }
    }

    private void onMemorycardUnMounted(Context context) {
		// do not unload user defined functions.
		/*
		MFPAdapter.clear();
		AssetManager am = context.getAssets();
        LinkedList<String> listLibFilePaths = new LinkedList<String>();
		listLibFilePaths.clear();
		mMFPAdapter.GetPreDefLibFiles(am, MFPAdapter.STRING_ASSET_SCRIPT_LIB_FOLDER, listLibFilePaths);
		mMFPAdapter.LoadLib(listLibFilePaths, am);	// load developer defined lib.
		mMFPAdapter.LoadInternalFuncInfo(am, MFPAdapter.STRING_ASSET_INTERNAL_FUNC_INFO_FILE);*/

    }

    private void onMemcardMounted(Context context) {
    	// interrupt current operations.
    	FunctionInterrupter prevfuncInterrupter = FuncEvaluator.msfunctionInterrupter;
    	ScriptInterrupter prevscriptInterrupter = ScriptAnalyzer.msscriptInterrupter;
    	AbstractExprInterrupter prevabstractExprInterrupter = AbstractExpr.msaexprInterrupter;
		FuncEvaluator.msfunctionInterrupter = new StorageActionFunctionInterrupter();
		ScriptAnalyzer.msscriptInterrupter = new StorageActionScriptInterrupter();
		AbstractExpr.msaexprInterrupter = new StorageActionAbstractExprInterrupter();
    	
		// reload everything
		CharSequence text = context.getString(R.string.app_name) + ": " + context.getString(R.string.mounting_external_storage);
		if (MFPAdapter.isFuncSpaceEmpty() == false && MFPAdapter.canReloadAll())	{
			// has to be synchronous mode because the following statement should run after reloading finishes.
			MFPAdapter.reloadAll(context.getApplicationContext(), -1, text.toString());
		}

		// everything has been back to normal so restore the previous interrupters.
		FuncEvaluator.msfunctionInterrupter = prevfuncInterrupter;
		ScriptAnalyzer.msscriptInterrupter = prevscriptInterrupter;
		AbstractExpr.msaexprInterrupter = prevabstractExprInterrupter;
    }

}
