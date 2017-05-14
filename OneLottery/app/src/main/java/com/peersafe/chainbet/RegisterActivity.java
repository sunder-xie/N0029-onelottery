package com.peersafe.chainbet;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.ui.BasicActivity;
import com.peersafe.chainbet.ui.setting.WifiBackRestoreActivity;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.PermissionUtils;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.netstate.NetworkUtil;
import com.peersafe.chainbet.widget.AlertDialog;
import com.peersafe.chainbet.widget.ImportDialog;
import com.peersafe.chainbet.widget.KeyboardChangeListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class RegisterActivity extends BasicActivity implements View.OnClickListener
{
    private static final int EMPORT_SD_FILE = 5;
    private int EMPORT_FILE_FLAG = 1;
    private int IMPORT_FILE_FLAG = 2;

    // 用户名输入框
    private EditText mRegisterName;

    // 密码输入框
    private EditText mRegisterPassword;

    // 确认密码输入框
    private EditText mConfirmRegisterPassword;

    // 父布局
    private LinearLayout mLinearLayout;

    // 登陆按钮
    private Button mLoginBtn;
    private TextView mLine;

    public static RegisterActivity instance = null;
    // 游客进入了注册，再点击逛逛。
    private boolean isVisitor = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        EventBus.getDefault().register(this);

        instance = this;

        isVisitor = getIntent().getBooleanExtra(ConstantCode.CommonConstant.TYPE, false);
        initView();

        // 设置inputType为password的时候，显示的文本不一致
        mRegisterPassword.setTypeface(Typeface.DEFAULT);
        mRegisterPassword.setTransformationMethod(new PasswordTransformationMethod());
        mConfirmRegisterPassword.setTypeface(Typeface.DEFAULT);
        mConfirmRegisterPassword.setTransformationMethod(new PasswordTransformationMethod());

        setKeyboardChangeLister();

        List<String> userList = UserInfoDBHelper.getInstance().getUserList(true);
        if (userList.isEmpty())
        {
            mLoginBtn.setVisibility(View.GONE);
            mLine.setVisibility(View.GONE);
        }
    }

    private void initView()
    {
        mRegisterName = (EditText) findViewById(R.id.et_register_user_name);
        mRegisterPassword = (EditText) findViewById(R.id.et_register_user_password);
        mConfirmRegisterPassword = (EditText) findViewById(R.id.et_confirm_register_user_password);
        mLinearLayout = (LinearLayout) findViewById(R.id.activity_register);
        mLoginBtn = (Button) findViewById(R.id.btn_register_login);
        mLine = (TextView) findViewById(R.id.line);

        findViewById(R.id.btn_register).setOnClickListener(this);
        findViewById(R.id.btn_register_see_first).setOnClickListener(this);
        findViewById(R.id.btn_register_import_new_account).setOnClickListener(this);
        mLoginBtn.setOnClickListener(this);

        mRegisterPassword.setFilters(new InputFilter[]{StringUtils.getFilter(),
                new InputFilter.LengthFilter(16)});
        mConfirmRegisterPassword.setFilters(new InputFilter[]{StringUtils.getFilter(),
                new InputFilter.LengthFilter(16)});
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_register:

                final String name = String.valueOf(mRegisterName.getText());
                final String password1 = String.valueOf(mRegisterPassword.getText()).trim();
                String password2 = String.valueOf(mConfirmRegisterPassword.getText()).trim();

                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if (!OneLotteryManager.isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                if (StringUtils.isEmpty(name))
                {
                    showToast(getString(R.string.common_enter_user_name));
                    mRegisterName.requestFocus();
                    break;
                }

                if (name.length() < 2)
                {
                    showToast(getString(R.string.register_name_length_too_short));
                    break;
                }

                if (StringUtils.isEmpty(password1))
                {
                    showToast(getString(R.string.common_enter_password));
                    mRegisterPassword.requestFocus();
                    break;
                }

                if (password1.length() < 8)
                {
                    showToast(getString(R.string.register_password_length_not_correct));
                    mRegisterPassword.requestFocus();
                    break;
                }

                if (StringUtils.isEmpty(password2))
                {
                    showToast(getString(R.string.register_enter_confirm_password));
                    mConfirmRegisterPassword.requestFocus();
                    break;
                }

                if (!password1.equals(password2))
                {
                    showToast(getString(R.string.register_password_not_the_same));
                    mConfirmRegisterPassword.setText("");
                    mConfirmRegisterPassword.requestFocus();
                    break;
                }

                showProgressDialog(getString(R.string.register_waiting), false);
                // 注册
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final long l = OneLotteryApi.registerAndEnroll(name, password1);
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                dismissProgressDialog();
                                if (l == 0)
                                {
                                    hideSoftKeyboard();
                                    UserInfo userInfo = new UserInfo();
                                    userInfo.setUserId(name);
                                    userInfo.setWalletAddr(OneLotteryApi.getPubkeyHash(name));
                                    userInfo.setBalance(0l);  //设置假数据

                                    UserInfoDBHelper.getInstance().setCurPrimaryAccount(userInfo);

                                    Intent intent = new Intent(RegisterActivity.this, AlertDialog
                                            .class);
                                    intent.putExtra(AlertDialog.TIP_TITLE, getString(R.string
                                            .register_success));
                                    intent.putExtra(AlertDialog.TIP_MESSAGE, getString(R.string
                                            .emport_file_alert_message));
                                    intent.putExtra(AlertDialog.SHOW_SKIP_BTN, true);
                                    intent.putExtra(AlertDialog.SHOW_EMPORT_BTN, true);
                                    startActivityForResult(intent, EMPORT_FILE_FLAG);

                                    OneLotteryApi.setCurUserId(name);
                                } else if (l == -3)
                                {
                                    showToast(getString(R.string.register_account_already_exists));
                                    mRegisterName.requestFocus();
                                    return;
                                } else
                                {
                                    showToast(getString(R.string.register_fail));
                                }
                            }
                        });
                    }
                }).start();
                break;

            case R.id.btn_register_login:
                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if (!OneLotteryManager.isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra(ConstantCode.CommonConstant.TYPE, isVisitor);
                startActivity(intent);
                finish();
                break;

            case R.id.btn_register_import_new_account:

                boolean result = checkPermissions(new String[]{PermissionUtils.PERMISSION_WRITE_EXTERNAL_STORAGE,PermissionUtils.PERMISSION_READ_EXTERNAL_STORAGE});
                if (result)
                {
                    ImportDialog dialog = new ImportDialog(RegisterActivity.this, ImportDialog.IMPORT_FILE, ImportDialog.CALL_IMPORT);
                    dialog.show();
                } else
                {
                    requestPermisson(this);
                }
                break;

            case R.id.btn_register_see_first:
                if (!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if (!OneLotteryManager.isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                if (isVisitor)
                {
                    finish();
                } else
                {
                    // TODO 不应该赋值为游客
                    OneLotteryApi.setCurUserId(ConstantCode.CommonConstant
                            .ONELOTTERY_DEFAULT_USERNAME);
                    String uid = OneLotteryApi.getCurUserId();
                    Intent mainActivity = new Intent(this, MainActivity.class);
                    startActivity(mainActivity);
                    finish();
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            if (requestCode == EMPORT_FILE_FLAG)
            {
                String stringType = data != null ? data.getStringExtra(AlertDialog
                        .SKIP_BUTTON_TYPE) : "";
                if (!StringUtils.isEmpty(stringType) && stringType.equals(AlertDialog
                        .SKIP_BUTTON_TYPE))
                {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else
                {
                    ImportDialog dialog = new ImportDialog(RegisterActivity.this, ImportDialog.EXPORT_FILE, ImportDialog.ENTER_MAIN);
                    dialog.show();
                }
            } else if (requestCode == IMPORT_FILE_FLAG)
            {
                boolean importOK = data != null ? data.getBooleanExtra(WifiBackRestoreActivity
                        .KEY_IMPORT_RSULT, false) : false;
                if (importOK)
                {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            else if(requestCode == EMPORT_SD_FILE)
            {
                Intent main = new Intent(RegisterActivity.this,MainActivity.class);
                startActivity(main);
            }
        }
    }

    private void setKeyboardChangeLister()
    {
        KeyboardChangeListener listener = new KeyboardChangeListener(this);
        listener.setKeyBoardListener(new KeyboardChangeListener.KeyBoardListener()
        {
            @Override
            public void onKeyboardChange(boolean isShow, int keyboardHeight)
            {
                if (!isShow || keyboardHeight < 101)
                {
                    mLinearLayout.setPadding(0, 0, 0, 0);
                } else
                {
                    int height = getWindowManager().getDefaultDisplay().getHeight();
                    mLinearLayout.setPadding(0, -height / 3, 0, 0);
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(final OLMessageModel model)
    {
        switch (model.getEventType())
        {
            case OLMessageModel.STMSG_MODEL_EXPORT_SD_FILE:
                boolean result = (boolean) model.getEventObject();
                if (result)
                {
                    Intent main = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(main);
                    finish();
                } else
                {
                    Intent intent = new Intent(RegisterActivity.this, AlertDialog.class);
                    intent.putExtra(AlertDialog.TIP_TITLE,getString(R.string.setting_file_emport_fail));
                    intent.putExtra(AlertDialog.TIP_MESSAGE, getString(R.string.setting_file_emport_fail_message));
                    intent.putExtra(AlertDialog.SHOW_CONFIRM_BTN,true);
                    startActivityForResult(intent, EMPORT_SD_FILE);
                }
                break;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
