package com.mcal.disassembler.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.mcal.disassembler.R;
import com.mcal.disassembler.nativeapi.Dumper;
import com.mcal.disassembler.util.FileSaver;
import com.mcal.disassembler.view.FloatingButton;
import com.mcal.materialdesign.view.CenteredToolBar;
import com.mcal.materialdesign.widgets.SnackBar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SymbolsActivity extends AppCompatActivity {
    private List<Map<String, Object>> data;
    private String path;
    private ProgressDialog mDialog;
    private SnackBar mBar;

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mDialog != null)
                mDialog.dismiss();
            mDialog = null;
            if (mBar != null)
                mBar.show();
            else
                new SnackBar(SymbolsActivity.this, SymbolsActivity.this.getString(R.string.done)).show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.symbols_activity);
        setupToolbar(getString(R.string.app_symbols));
        ListView list = findViewById(R.id.symbols_activity_list_view);
        data = getData();
        SymbolsAdapter adapter = new SymbolsAdapter(this);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new ItemClickListener());

        path = Objects.requireNonNull(getIntent().getExtras()).getString("filePath");
    }

    @SuppressWarnings("ConstantConditions")
    private void setupToolbar(String title) {
        CenteredToolBar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @NotNull
    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map;
        for (int i = 0; i < Dumper.symbols.size(); ++i) {
            map = new HashMap<>();
            if (Dumper.symbols.get(i).getType() == 1)
                map.put("img", R.drawable.ic_box_blue);
            else if (Dumper.symbols.get(i).getType() == 2)
                map.put("img", R.drawable.ic_box_red);
            else map.put("img", R.drawable.ic_box_green);
            map.put("title", Dumper.symbols.get(i).getDemangledName());
            map.put("info", Dumper.symbols.get(i).getName());
            map.put("type", Dumper.symbols.get(i).getType());
            list.add(map);
        }
        return list;
    }

    public void showFloatingMenu(View view) {
        new FloatingButton(this, path).show();
    }

    public void showSearch(View view) {
        Intent i = new Intent(this, SearchActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("filePath", path);
        i.putExtras(bundle);
        startActivity(i);
    }

    private void _saveSymbols() {
        String[] strings = new String[Dumper.symbols.size()];
        for (int i = 0; i < Dumper.symbols.size(); ++i)
            strings[i] = Dumper.symbols.get(i).getName();

        FileSaver saver = new FileSaver(Environment.getExternalStorageDirectory().toString() + "/Disassembler/symbols/", "Symbols.txt", strings);
        saver.save();

        String[] strings_ = new String[Dumper.symbols.size()];
        for (int i = 0; i < Dumper.symbols.size(); ++i)
            strings_[i] = Dumper.symbols.get(i).getDemangledName();
        FileSaver saver_ = new FileSaver(Environment.getExternalStorageDirectory().toString() + "/Disassembler/symbols/", "Symbols_demangled.txt", strings_);
        saver_.save();
    }

    public void saveSymbols(View view) {
        mDialog = new ProgressDialog(this);
        mDialog.setTitle(getString(R.string.saving));
        mDialog.show();
        mBar = new SnackBar(this, getString(R.string.done));
        new Thread() {
            public void run() {
                _saveSymbols();
                Message msg = new Message();
                mHandler.sendMessage(msg);
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        SnackBar bar = new SnackBar(this, getString(R.string.againToExit));
        bar.show();
        bar.setDismissTimer(2500);
        bar.setOnBackPressedListener(SymbolsActivity.this::finish);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static class ViewHolder {
        public AppCompatTextView info;
        public int type;
        AppCompatImageView img;
        AppCompatTextView title;
    }

    private final class ItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, @NotNull View view, int arg2, long arg3) {
            Bundle bundle = new Bundle();
            bundle.putString("demangledName", (String) (((ViewHolder) view.getTag()).title.getText()));
            bundle.putString("name", (String) (((ViewHolder) view.getTag()).info.getText()));
            bundle.putInt("type", ((ViewHolder) view.getTag()).type);
            bundle.putString("filePath", path);
            Intent intent = new Intent(SymbolsActivity.this, SymbolActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    public class SymbolsAdapter extends BaseAdapter {
        private LayoutInflater mInflater = null;

        private SymbolsAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.symbol_list_item, null);
                holder.img = convertView.findViewById(R.id.symbolslistitemimg);
                holder.title = convertView.findViewById(R.id.symbolslistitemTextViewtop);
                holder.info = convertView.findViewById(R.id.symbolslistitemTextViewbottom);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.img.setBackgroundResource((Integer) data.get(position).get("img"));
            holder.title.setText((String) data.get(position).get("title"));
            holder.info.setText((String) data.get(position).get("info"));
            holder.type = ((int) data.get(position).get("type"));

            return convertView;
        }
    }
}