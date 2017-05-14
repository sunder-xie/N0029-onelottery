package com.peersafe.chainbet;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import com.peersafe.chainbet.widget.ImportDialog;
import com.peersafe.chainbet.widget.KeyboardChangeListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends BasicActivity implements View.OnClickListener, AdapterView.OnItemClickListener
{
    private int IMPORT_FILE_FLAG = 2;

    public static LoginActivity instance = null;
    // 用户名输入框
    private EditText mLoginUserName;

    // 密码输入框
    private EditText mLoginPassword;

    // 页面父布局
    private LinearLayout mLoginLinerLay;

    // 登录选择用户按钮
    private ImageView mDropMenu;

    // 展示用户集合的listview
    private ListView mSelectUserList;

    // 用户输入框父布局
    private RelativeLayout mLoginUserNameRel;

    // 选择下拉框的popwindow
    private PopupWindow mSelectUserPop;

    // 用户数据集合
    private List userList = new ArrayList();

    private boolean isVisitor = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        instance = this;

        isVisitor = getIntent().getBooleanExtra(ConstantCode.CommonConstant.TYPE, false);

        initView();

        mLoginPassword.setTypeface(Typeface.DEFAULT);
        mLoginPassword.setTransformationMethod(new PasswordTransformationMethod());

        setKeyboardChangeLister();

        UserInfo pri = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        if (pri != null)
        {
            userList.add(pri.getUserId());
        }
        userList.addAll(UserInfoDBHelper.getInstance().getUserList(false));
        if (!userList.isEmpty())
        {
            mLoginUserName.setText((CharSequence) userList.get(0));
        }
    }

    private void initView()
    {
        mLoginPassword = (EditText) findViewById(R.id.et_login_password);
        mLoginUserName = (EditText) findViewById(R.id.et_login_user_name);
        mLoginLinerLay = (LinearLayout) findViewById(R.id.activity_login);
        mDropMenu = (ImageView) findViewById(R.id.login_select_drop_menu);
        mLoginUserNameRel = (RelativeLayout) findViewById(R.id.login_user_name_relayout);

        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.btn_register).setOnClickListener(this);
        findViewById(R.id.btn_import_new_account).setOnClickListener(this);
        findViewById(R.id.btn_see_first).setOnClickListener(this);
        mDropMenu.setOnClickListener(this);

        mLoginPassword.setFilters(new InputFilter[]{StringUtils.getFilter(),
                new InputFilter.LengthFilter(16)});
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_login:

                final String name = String.valueOf(mLoginUserName.getText()).trim();
                final String password = String.valueOf(mLoginPassword.getText()).trim();

                if(!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if(!OneLotteryManager.getInstance().isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                if (StringUtils.isEmpty(password))
                {
                    showToast(getString(R.string.common_enter_password));
                    break;
                }

                showProgressDialog(getString(R.string.login_waiting), false);

                // 登录逻辑 解密文件
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final long login = OneLotteryApi.login(name, password);
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                dismissProgressDialog();
                                if (login == 0)
                                {
                                    UserInfo userInfo = UserInfoDBHelper.getInstance().getUserByUserId(name);
                                    UserInfoDBHelper.getInstance().setCurPrimaryAccount(userInfo);
                                    OneLotteryApi.setCurUserId(name);

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else
                                {
                                    mLoginPassword.setText("");
                                    showToast(getString(R.string.login_fail));
                                }
                            }
                        });

                    }
                }).start();
                break;

            case R.id.btn_register:
                if(!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if(!OneLotteryManager.isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                Intent register = new Intent(this, RegisterActivity.class);
                register.putExtra(ConstantCode.CommonConstant.TYPE, isVisitor);
                startActivity(register);
                finish();
                break;

            case R.id.btn_import_new_account:

                boolean result = checkPermissions(new String[]{PermissionUtils.PERMISSION_WRITE_EXTERNAL_STORAGE,PermissionUtils.PERMISSION_READ_EXTERNAL_STORAGE});
                if (result)
                {
                    ImportDialog dialog = new ImportDialog(this, ImportDialog.IMPORT_FILE, ImportDialog.CALL_IMPORT);
                    dialog.show();
                } else
                {
                    requestPermisson(this);
                }
                break;

            case R.id.btn_see_first:
                if(!NetworkUtil.isNetworkConnected())
                {
                    showToast(getString(R.string.check_network));
                    break;
                }

                if(!OneLotteryManager.getInstance().isServiceConnect)
                {
                    showToast(getString(R.string.check_service));
                    break;
                }

                if (isVisitor)
                {
                    finish();
                } else
                {
                    OneLotteryApi.setCurUserId(ConstantCode.CommonConstant
                            .ONELOTTERY_DEFAULT_USERNAME);
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;

            case R.id.login_select_drop_menu:
                if (mSelectUserPop != null && mSelectUserPop.isShowing())
                {
                    mSelectUserPop.dismiss();
                    mDropMenu.setImageResource(R.drawable.login_drop_icon);
                } else
                {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context
                            .LAYOUT_INFLATER_SERVICE);
                    view = inflater.inflate(R.layout.login_select_pop, null);
                    mSelectUserList = (ListView) view.findViewById(R.id.login_pop_list_view);
                    ArrayAdapter mSelectUserAdapter = new ArrayAdapter(this, R.layout
                            .login_select_user_menu_item, userList);
                    mSelectUserList.setAdapter(mSelectUserAdapter);

                    mDropMenu.setImageResource(R.drawable.login_up_icon);

                    mSelectUserPop = new PopupWindow(view, mLoginUserNameRel.getWidth(),
                            mLoginUserNameRel.getHeight() * (userList.size() > 4 ? 4 : userList.size()));
                    mSelectUserPop.showAsDropDown(mLoginUserNameRel);

                    mSelectUserList.setOnItemClickListener(this);
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
            if (requestCode == IMPORT_FILE_FLAG)
            {
                boolean importOK = data != null ? data.getBooleanExtra(WifiBackRestoreActivity.KEY_IMPORT_RSULT, false) : false;

                if (importOK)
                {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        String o = (String) userList.get(i);
        mLoginUserName.setText(o);
        dismissPopWindow();
    }

    private void dismissPopWindow()
    {
        mDropMenu.setImageResource(R.drawable.login_drop_icon);
        if (mSelectUserPop != null && mSelectUserPop.isShowing())
        {
            mSelectUserPop.dismiss();
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
                int height = getWindowManager().getDefaultDisplay().getHeight();
                if (!isShow || keyboardHeight < 101)
                {
                    dismissPopWindow();
                    mLoginLinerLay.setPadding(0, 0, 0, 0);
                } else
                {
                    dismissPopWindow();
                    mLoginLinerLay.setPadding(0, -height / 4, 0, 0);
                }
            }
        });
    }
}
