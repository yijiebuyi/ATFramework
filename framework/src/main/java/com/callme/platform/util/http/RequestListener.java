package com.callme.platform.util.http;

import android.app.Activity;
import android.text.TextUtils;

import com.callme.platform.R;
import com.callme.platform.base.BaseActivity;
import com.callme.platform.base.BaseBusiness;
import com.callme.platform.base.BaseFragment;
import com.callme.platform.base.BaseResponseBean;
import com.callme.platform.util.ApnUtil;
import com.callme.platform.util.CmRequestListener;
import com.callme.platform.util.SharedPreferencesUtil;
import com.callme.platform.util.ToastUtil;
import com.callme.platform.util.cookie.CookieManager;
import com.callme.platform.util.http.core.HttpResponse;

import org.json.JSONObject;

public class RequestListener<T> {
    public final static String USER_NAME = "userName"; // 用户账号
    public final static String USER_PWD = "userPwd"; // 用户密码
    private final int CODE_SUCCESS = 0;
    private final int CODE_FAILED = 1;
    private final int CODE_UPDATE = -10;
    private final int CODE_INVALID = 501;
    private RequestCallBack<T> callback;
    private CmRequestListener<T> mListener;
    private Object comeFrom;
    private static boolean hasShown = false;

    /**
     * from:listener使用的地方， 只能用于BaseActivity或BaseFragment子类
     *
     * @param from
     */
    public RequestListener(CmRequestListener cmReq) {
        mListener = cmReq;
        comeFrom = cmReq.getComeFrom();
        callback = new RequestCallBack<T>() {

            @Override
            public void onStart() {
                mListener.onStart();
            }

            @Override
            public void onReSendReq() {
                mListener.onReSendReq();
            }

            @Override
            public void onLoginTimeout() {
                mListener.onLoginTimeout();
            }

            @Override
            public void onCancelled() {
                mListener.onCancelled();
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                mListener.onLoading(total, current, isUploading);
            }

            @Override
            public void onSuccess(ResponseInfo<T> responseInfo) {
                checkReponse(responseInfo);
            }

            @Override
            public void onFailure(int exceptionCode, String msg) {
                if (HttpResponse.SC_UNAUTHORIZED == exceptionCode) {
                    if (comeFrom instanceof BaseActivity) {
                        ((BaseActivity) comeFrom).closeProgress();
                        ((BaseActivity) comeFrom).closeProgressDialog();
                        mListener.onCancelled();
                        showReLogin((BaseActivity) comeFrom);
                    } else if (comeFrom instanceof BaseFragment) {
                        ((BaseFragment) comeFrom).closeProgress();
                        ((BaseFragment) comeFrom).closeProgressDialog();
                        mListener.onCancelled();
                        showReLogin(((BaseFragment) comeFrom).getActivity());
                    } else {
                        mListener.onFailure(exceptionCode, msg);
                    }
                } else {
                    mListener.onFailure(exceptionCode, msg);
                }
            }
        };
    }

    public void autoLogin(final Activity context, final String phone,
                          final String pwd) {
//		LoginBusiness.login(context, phone, pwd, true,
//				new RequestListener<JSONObject>(this) {
//
//					@Override
//					public void onSuccess(JSONObject result) {
//						if (SystemConsts.FORCE_RSA_ENCRYPT) {
//							SystemConsts.RSA_ENCRYPT = true;
//						}
//
//						SharedPreferencesUtil spUtil = SharedPreferencesUtil
//								.getInstance(context);
//						spUtil.putString(SystemConsts.USER_NAME, phone);
//						spUtil.putString(SystemConsts.USER_PWD, pwd);
//
//						mListener.onReSendReq();
//					}
//
//					@Override
//					public void onFailure(int exceptionCode, String msg) {
//						if(EncryptUtil.handleEncryptResponse(context, msg, phone, pwd, true)) {
//							autoLogin(context, phone, pwd);
//						} else {
//							ToastUtil.showCustomViewToast(context,
//									BaseBusiness.getResponseMsg(msg));
//						}
//					}
//				});
    }

    private void showReLogin(final Activity context) {
        CookieManager.getInstance().clearCookies();
        if (context == null || context.isFinishing() || hasShown) {
            return;
        }
        // cookie超时的情况自动登录
        final SharedPreferencesUtil mspUtil = SharedPreferencesUtil
                .getInstance(context);
        final String phone = mspUtil.getString(USER_NAME, "");
        final String pwd = mspUtil.getString(USER_PWD, "");
        ToastUtil.showCustomViewToast(context, R.string.re_logining);
        if (ApnUtil.isNetworkAvailable(context) && !TextUtils.isEmpty(phone)
                && !TextUtils.isEmpty(pwd)) {
            autoLogin(context, phone, pwd);
            return;
        }

        if (context instanceof BaseActivity) {
//			mspUtil.putString(SystemConsts.USERINFO_KEY, "");
//			SystemConsts.mLoginInfo = null;
//			Intent intent = new Intent(context, LoginActivity.class);
//			context.startActivity(intent);
        }
    }

    private final void checkReponse(ResponseInfo<T> responseInfo) {
        try {
            if (responseInfo != null) {
                if (responseInfo.result instanceof JSONObject) {

                    BaseResponseBean bean = BaseBusiness
                            .paseBase(String.valueOf(responseInfo.result));
                    if (bean != null) {
                        switch (bean.error) {
                            case CODE_SUCCESS:
                                mListener.onSuccess((JSONObject) responseInfo.result);
                                break;
                            case CODE_FAILED:
                                mListener.onFailure(responseInfo.statusCode, bean.message);
                            case CODE_UPDATE:
                                if (comeFrom instanceof BaseActivity) {
                                    ((BaseActivity) comeFrom).closeProgress();
                                    ((BaseActivity) comeFrom).closeProgressDialog();
//                                    showUpdate((BaseActivity) comeFrom, bean.Desc,
//                                            bean.Data.toString());
                                } else if (comeFrom instanceof BaseFragment) {
                                    ((BaseFragment) comeFrom).closeProgress();
                                    ((BaseFragment) comeFrom).closeProgressDialog();
//                                    showUpdate(
//                                            ((BaseFragment) comeFrom).getActivity(),
//                                            bean.Desc, bean.Data.toString());
                                } else {
                                    onFailure(responseInfo.statusCode,
                                            responseInfo.result.toString());
                                }
                                break;
                            case CODE_INVALID:
                                if (comeFrom instanceof BaseActivity) {
                                    ((BaseActivity) comeFrom).closeProgress();
                                    ((BaseActivity) comeFrom).closeProgressDialog();
                                    mListener.onLoginTimeout();
                                    showReLogin((BaseActivity) comeFrom);
                                } else if (comeFrom instanceof BaseFragment) {
                                    ((BaseFragment) comeFrom).closeProgress();
                                    ((BaseFragment) comeFrom).closeProgressDialog();
                                    mListener.onLoginTimeout();
                                    showReLogin(((BaseFragment) comeFrom)
                                            .getActivity());
                                } else {
                                    mListener.onFailure(responseInfo.statusCode,
                                            responseInfo.result.toString());
                                }
                                break;
                            default:
                                onFailure(responseInfo.statusCode,
                                        responseInfo.result.toString());
                                break;
                        }
                    } else {
                        onFailure(responseInfo.statusCode,
                                responseInfo.result.toString());
                    }
                } else if (responseInfo.result instanceof String) {
                    JSONObject json = new JSONObject(
                            (String) responseInfo.result);
                    BaseResponseBean bean = BaseBusiness
                            .paseBase(String.valueOf(responseInfo.result));
                    if (bean != null) {
                        switch (bean.error) {
                            case CODE_SUCCESS:
                                mListener.onSuccess(json);
                                break;
                            case CODE_FAILED:
                                mListener.onFailure(responseInfo.statusCode, bean.message);
                                break;
                            case CODE_UPDATE:
                                if (comeFrom instanceof BaseActivity) {
                                    ((BaseActivity) comeFrom).closeProgress();
                                    ((BaseActivity) comeFrom).closeProgressDialog();
//                                    showUpdate((BaseActivity) comeFrom, bean.Desc,
//                                            bean.Data.toString());
                                } else if (comeFrom instanceof BaseFragment) {
                                    ((BaseFragment) comeFrom).closeProgress();
                                    ((BaseFragment) comeFrom).closeProgressDialog();
//                                    showUpdate(
//                                            ((BaseFragment) comeFrom).getActivity(),
//                                            bean.Desc, bean.Data.toString());
                                } else {
                                    onFailure(responseInfo.statusCode,
                                            responseInfo.result.toString());
                                }
                                break;
                            case CODE_INVALID:
                                if (comeFrom instanceof BaseActivity) {
                                    ((BaseActivity) comeFrom).closeProgress();
                                    ((BaseActivity) comeFrom).closeProgressDialog();
                                    mListener.onLoginTimeout();
                                    showReLogin((BaseActivity) comeFrom);
                                } else if (comeFrom instanceof BaseFragment) {
                                    ((BaseFragment) comeFrom).closeProgress();
                                    ((BaseFragment) comeFrom).closeProgressDialog();
                                    mListener.onLoginTimeout();
                                    showReLogin(((BaseFragment) comeFrom)
                                            .getActivity());
                                } else {
                                    mListener.onFailure(responseInfo.statusCode,
                                            responseInfo.result.toString());
                                }
                                break;
                            default:
                                onFailure(responseInfo.statusCode,
                                        responseInfo.result.toString());
                                break;
                        }
                    } else {
                        onFailure(responseInfo.statusCode,
                                responseInfo.result.toString());
                    }
                }
            } else {
                onFailure(-1000, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            onFailure(responseInfo.statusCode, responseInfo.result.toString());
        }

    }

    public void showUpdate(final Activity context, String msg,
                           final String downloadUrl) {
        if (context.isFinishing() || hasShown) {
            return;
        }
        hasShown = true;
//		final CmDialog dialog = new CmDialog(context, msg,
//				R.string.app_update);
//		dialog.setOnCancelListener(new OnCancelListener() {
//
//			@Override
//			public void onCancel(DialogInterface dialog) {
//				hasShown = false;
//			}
//		});
//		dialog.setOnDismissListener(new OnDismissListener() {
//
//			@Override
//			public void onDismiss(DialogInterface dialog) {
//				hasShown = false;
//			}
//		});
//		dialog.setNegativeButton(R.string.delay_update,
//				new DialogOnClickListener() {
//
//					@Override
//					public void onClick() {
//						dialog.dismiss();
//					}
//				});
//		dialog.setPositiveButton(R.string.right_now_update,
//				new DialogOnClickListener() {
//
//					@Override
//					public void onClick() {
//						dialog.dismiss();
//						Intent intent = new Intent(context,
//								DownloadService.class);
//						intent.putExtra(DownloadService.KEY_DOWNLOAD_URL,
//								downloadUrl);
//						context.startService(intent);
//					}
//				});
//		dialog.show();
    }

    public RequestCallBack<T> getCallBack() {
        return callback;
    }

    public void onStart() {
    }

    public void onCancelled() {
    }

    public void onLoginTimeout() {
    }

    public void onReSendReq() {
    }

    public void onLoading(long total, long current, boolean isUploading) {
    }

    public void onSuccess(T response) {
    }

    public void onFailure(int exceptionCode, String response) {
    }

}
