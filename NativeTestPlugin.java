package com.uin.testplugin;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.os.Bundle;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.UIN.Tool.plugin.PluginInterface;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NativeTestPlugin implements PluginInterface {

    private Activity hostActivity;
    private Context pluginContext;
    private LinearLayout mainLayout;
    private TextView tvInfo;
    private TextView tvDevice;
    private TextView tvTime;

    @Override
    public View onCreateView(Context context, ViewGroup container, Bundle savedInstanceState) {
        // 保存插件 Context（用于创建 View）
        this.pluginContext = context;
        
        // 尝试从 container 获取 Activity
        if (container != null && container.getContext() instanceof Activity) {
            this.hostActivity = (Activity) container.getContext();
        }
        
        // 如果还是 null，尝试从 context 获取（虽然 PluginContext 不是 Activity，但可能包装了 Activity）
        if (this.hostActivity == null && context instanceof Activity) {
            this.hostActivity = (Activity) context;
        }
        
        // 最终降级方案：使用 context 本身（虽然可能有问题，但至少不崩溃）
        if (this.hostActivity == null) {
            // 创建一个临时的 Activity 引用是不可能的，这里记录错误
            android.util.Log.e("NativeTestPlugin", "无法获取 Activity Context，Dialog 功能将不可用");
        }

        // 创建滚动视图 - 使用 pluginContext 而不是 hostActivity
        ScrollView scrollView = new ScrollView(pluginContext);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        scrollView.setBackgroundColor(0xFFF5F5F5);

        // 主布局
        mainLayout = new LinearLayout(pluginContext);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 40, 40, 40);
        mainLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // ==================== 标题 ====================
        TextView title = new TextView(pluginContext);
        title.setText("🧪 原生插件测试面板");
        title.setTextSize(26);
        title.setTextColor(0xFF37474F);
        title.setPadding(0, 0, 0, 40);
        title.setGravity(android.view.Gravity.CENTER);
        mainLayout.addView(title);

        // ==================== 1. 插件信息区域 ====================
        addSectionTitle("📱 插件信息");
        
        tvInfo = new TextView(pluginContext);
        tvInfo.setTextSize(14);
        tvInfo.setTextColor(0xFF666666);
        tvInfo.setBackgroundColor(0xFFEEEEEE);
        tvInfo.setPadding(20, 20, 20, 20);
        tvInfo.setText("点击下方按钮获取插件信息");
        mainLayout.addView(tvInfo);
        
        LinearLayout infoBtnRow = createButtonRow();
        Button btnGetInfo = createButton("获取插件信息", 0xFF2196F3);
        btnGetInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testGetPluginInfo();
            }
        });
        infoBtnRow.addView(btnGetInfo);
        mainLayout.addView(infoBtnRow);

        addDivider();

        // ==================== 2. 设备信息区域 ====================
        addSectionTitle("📊 设备信息");
        
        tvDevice = new TextView(pluginContext);
        tvDevice.setTextSize(14);
        tvDevice.setTextColor(0xFF666666);
        tvDevice.setBackgroundColor(0xFFEEEEEE);
        tvDevice.setPadding(20, 20, 20, 20);
        tvDevice.setText("点击下方按钮获取设备信息");
        mainLayout.addView(tvDevice);
        
        LinearLayout deviceBtnRow = createButtonRow();
        Button btnGetDevice = createButton("获取设备信息", 0xFF2196F3);
        btnGetDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testGetDeviceInfo();
            }
        });
        deviceBtnRow.addView(btnGetDevice);
        mainLayout.addView(deviceBtnRow);

        addDivider();

        // ==================== 3. 时间信息区域 ====================
        addSectionTitle("⏰ 时间信息");
        
        tvTime = new TextView(pluginContext);
        tvTime.setTextSize(14);
        tvTime.setTextColor(0xFF666666);
        tvTime.setBackgroundColor(0xFFEEEEEE);
        tvTime.setPadding(20, 20, 20, 20);
        tvTime.setText("点击下方按钮获取当前时间");
        mainLayout.addView(tvTime);
        
        LinearLayout timeBtnRow = createButtonRow();
        Button btnGetTime = createButton("获取当前时间", 0xFF2196F3);
        btnGetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testGetTime();
            }
        });
        timeBtnRow.addView(btnGetTime);
        mainLayout.addView(timeBtnRow);

        addDivider();

        // ==================== 4. 提示功能区域 ====================
        addSectionTitle("📢 提示功能");
        
        LinearLayout toastRow = createButtonRow();
        Button shortToastBtn = createButton("短提示", 0xFF4CAF50);
        shortToastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("这是短提示消息");
            }
        });
        toastRow.addView(shortToastBtn);
        
        Button longToastBtn = createButton("长提示", 0xFF4CAF50);
        longToastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLongToast("这是长提示消息，会显示更长时间");
            }
        });
        toastRow.addView(longToastBtn);
        mainLayout.addView(toastRow);

        addDivider();

        // ==================== 5. 弹窗功能区域 ====================
        addSectionTitle("💬 弹窗功能");
        
        LinearLayout dialogRow = createButtonRow();
        Button alertBtn = createButton("弹窗", 0xFFFF9800);
        alertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlert();
            }
        });
        dialogRow.addView(alertBtn);
        
        Button confirmBtn = createButton("确认框", 0xFFFF9800);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirm();
            }
        });
        dialogRow.addView(confirmBtn);
        mainLayout.addView(dialogRow);

        addDivider();

        // ==================== 6. 震动功能区域 ====================
        addSectionTitle("📳 震动功能");
        
        LinearLayout vibrateRow = createButtonRow();
        Button vibrateBtn = createButton("震动200ms", 0xFF9C27B0);
        vibrateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrate();
            }
        });
        vibrateRow.addView(vibrateBtn);
        
        Button vibrateLongBtn = createButton("震动500ms", 0xFF9C27B0);
        vibrateLongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrateLong();
            }
        });
        vibrateRow.addView(vibrateLongBtn);
        mainLayout.addView(vibrateRow);

        addDivider();

        // ==================== 7. 剪贴板功能区域 ====================
        addSectionTitle("📋 剪贴板功能");
        
        LinearLayout copyRow = createButtonRow();
        Button copyBtn = createButton("复制文本", 0xFF3F51B5);
        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyText();
            }
        });
        copyRow.addView(copyBtn);
        
        Button pasteBtn = createButton("粘贴", 0xFF3F51B5);
        pasteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pasteText();
            }
        });
        copyRow.addView(pasteBtn);
        mainLayout.addView(copyRow);

        addDivider();

        // ==================== 8. 系统功能区域 ====================
        addSectionTitle("⚙️ 系统功能");
        
        LinearLayout systemRow1 = createButtonRow();
        Button settingsBtn = createButton("系统设置", 0xFF607D8B);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });
        systemRow1.addView(settingsBtn);
        
        Button appSettingsBtn = createButton("应用设置", 0xFF607D8B);
        appSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAppSettings();
            }
        });
        systemRow1.addView(appSettingsBtn);
        mainLayout.addView(systemRow1);

        LinearLayout systemRow2 = createButtonRow();
        Button shareBtn = createButton("分享", 0xFF607D8B);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareText();
            }
        });
        systemRow2.addView(shareBtn);
        
        Button closeBtn = createButton("关闭插件", 0xFFF44336);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePlugin();
            }
        });
        systemRow2.addView(closeBtn);
        mainLayout.addView(systemRow2);

        // 底部留白
        View spacer = new View(pluginContext);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                80
        ));
        mainLayout.addView(spacer);

        scrollView.addView(mainLayout);
        return scrollView;
    }

    private void addSectionTitle(String text) {
        TextView sectionTitle = new TextView(pluginContext);
        sectionTitle.setText(text);
        sectionTitle.setTextSize(18);
        sectionTitle.setTextColor(0xFF37474F);
        sectionTitle.setPadding(0, 20, 0, 15);
        sectionTitle.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        mainLayout.addView(sectionTitle);
    }

    private void addDivider() {
        View divider = new View(pluginContext);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        ));
        divider.setBackgroundColor(0xFFDDDDDD);
        divider.setPadding(0, 15, 0, 15);
        mainLayout.addView(divider);
    }

    private LinearLayout createButtonRow() {
        LinearLayout layout = new LinearLayout(pluginContext);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        return layout;
    }

    private Button createButton(String text, int color) {
        Button button = new Button(pluginContext);
        button.setText(text);
        button.setTextSize(13);
        button.setTextColor(0xFFFFFFFF);
        button.setBackgroundColor(color);
        button.setPadding(15, 12, 15, 12);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        params.setMargins(0, 0, 15, 10);
        button.setLayoutParams(params);
        return button;
    }

    // ==================== 功能实现方法 ====================

    private void testGetPluginInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("📌 插件名称：原生测试插件\n");
        sb.append("📌 插件类型：原生代码 UI\n");
        sb.append("📌 功能说明：测试原生插件的各项功能\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("\n✅ 原生插件可以直接调用 Android API\n");
        sb.append("✅ 无需 WebView，性能最优\n");
        sb.append("✅ 可以访问所有系统功能");
        tvInfo.setText(sb.toString());
        showToast("插件信息已更新");
    }

    private void testGetDeviceInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("📱 品牌：").append(Build.BRAND).append("\n");
        sb.append("📱 型号：").append(Build.MODEL).append("\n");
        sb.append("📱 制造商：").append(Build.MANUFACTURER).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("🤖 系统：Android ").append(Build.VERSION.RELEASE).append("\n");
        sb.append("🤖 API：").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("🤖 产品：").append(Build.PRODUCT);
        tvDevice.setText(sb.toString());
        showToast("设备信息已更新");
    }

    private void testGetTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String time = sdf.format(new Date());
        StringBuilder sb = new StringBuilder();
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("🕐 当前时间：\n");
        sb.append(time).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("\n📅 日期格式：yyyy-MM-dd\n");
        sb.append("⏰ 时间格式：HH:mm:ss");
        tvTime.setText(sb.toString());
        showToast("时间已更新");
    }

    private void showToast(String message) {
        Toast.makeText(pluginContext, message, Toast.LENGTH_SHORT).show();
    }

    private void showLongToast(String message) {
        Toast.makeText(pluginContext, message, Toast.LENGTH_LONG).show();
    }

    private void showAlert() {
        if (hostActivity == null || hostActivity.isFinishing()) {
            showToast("无法显示弹窗：Activity 不可用");
            return;
        }
        
        new android.app.AlertDialog.Builder(hostActivity)
                .setTitle("💡 提示")
                .setMessage("这是一个弹窗提示消息")
                .setPositiveButton("知道了", null)
                .show();
    }

    private void showConfirm() {
        if (hostActivity == null || hostActivity.isFinishing()) {
            showToast("无法显示确认框：Activity 不可用");
            return;
        }
        
        new android.app.AlertDialog.Builder(hostActivity)
                .setTitle("❓ 确认")
                .setMessage("请确认您的操作")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showToast("✓ 你点击了确定");
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showToast("✗ 你点击了取消");
                    }
                })
                .show();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) pluginContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(200);
            }
            showToast("📳 震动200ms");
        } else {
            showToast("不支持震动");
        }
    }

    private void vibrateLong() {
        Vibrator vibrator = (Vibrator) pluginContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500);
            }
            showToast("📳 震动500ms");
        } else {
            showToast("不支持震动");
        }
    }

    private void copyText() {
        ClipboardManager clipboard = (ClipboardManager) pluginContext.getSystemService(Context.CLIPBOARD_SERVICE);
        String textToCopy = "这是从原生插件复制的测试文本\n时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        ClipData clip = ClipData.newPlainText("plugin_text", textToCopy);
        clipboard.setPrimaryClip(clip);
        showToast("📋 已复制到剪贴板");
    }

    private void pasteText() {
        ClipboardManager clipboard = (ClipboardManager) pluginContext.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            String text = item.getText().toString();
            showToast("📋 剪贴板内容：" + (text.length() > 30 ? text.substring(0, 30) + "..." : text));
        } else {
            showToast("剪贴板为空");
        }
    }

    private void openSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        pluginContext.startActivity(intent);
    }

    private void openAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(android.net.Uri.parse("package:" + pluginContext.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        pluginContext.startActivity(intent);
    }

    private void shareText() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "通过原生插件分享的测试内容\n时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        pluginContext.startActivity(Intent.createChooser(intent, "分享"));
    }

    private void closePlugin() {
        if (hostActivity != null && !hostActivity.isFinishing()) {
            hostActivity.finish();
        }
    }

    // ==================== 生命周期方法 ====================

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public Bundle onSaveInstanceState() {
        return null;
    }
}