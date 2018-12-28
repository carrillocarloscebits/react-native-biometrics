package com.rnbiometrics;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Window;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.app.Dialog;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.rnbiometrics.R;

/**
 * Created by brandon on 4/6/18.
 */

@TargetApi(Build.VERSION_CODES.M)
public class ReactNativeBiometricsDialog extends DialogFragment implements ReactNativeBiometricsCallback {

    protected String title;
    protected FingerprintManager.CryptoObject cryptoObject;
    protected ReactNativeBiometricsCallback biometricAuthCallback;

    protected ReactNativeBiometricsHelper biometricAuthenticationHelper;
    protected Activity activity;
    protected Button cancelButton;
    protected TextView txtDesc, fingerPrintIcon;

    public void init(String title, FingerprintManager.CryptoObject cryptoObject, ReactNativeBiometricsCallback callback) {
        this.title = title;
        this.cryptoObject = cryptoObject;
        this.biometricAuthCallback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //if (title.equalsIgnoreCase("register"))
            //dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //getDialog().setTitle(title);
        View view = inflater.inflate(R.layout.fingerprint_dialog_container, container, false);
        cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setText(R.string.fingerprint_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissAllowingStateLoss();
                onCancel();
            }
        });

        txtDesc = (TextView) view.findViewById(R.id.dialogDescription);
        fingerPrintIcon = (TextView) view.findViewById(R.id.fingerPrintIcon);

        if (title.equalsIgnoreCase("register"))
            txtDesc.setText("Register your fingerprint");
        else
            txtDesc.setText("Log in with your fingerprint");

        biometricAuthenticationHelper = new ReactNativeBiometricsHelper(
                activity.getSystemService(FingerprintManager.class),
                (ImageView) view.findViewById(R.id.fingerprint_icon),
                (TextView) view.findViewById(R.id.fingerprint_status),
                this
        );

        return view;
    }

    // DialogFragment lifecycle methods
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    @Override
    public void onPause() {
        super.onPause();
        biometricAuthenticationHelper.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        biometricAuthenticationHelper.startListening(cryptoObject);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        onCancel();
    }

    // ReactNativeBiometricsCallback methods
    @Override
    public void onAuthenticated(FingerprintManager.CryptoObject cryptoObject) {
        if (title.equalsIgnoreCase("register")){
            txtDesc.setText("Fingerprint Registered");
            txtDesc.setTextColor(Color.parseColor("#09BA83"));
            fingerPrintIcon.setBackgroundResource(R.drawable.circle_shape_success);

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            dismissAllowingStateLoss();
                        }
                    },
                    1000);
        } else {
            dismissAllowingStateLoss();
        }


        if (biometricAuthCallback != null) {
            biometricAuthCallback.onAuthenticated(cryptoObject);
        }
    }

    @Override
    public void onCancel() {
        if (biometricAuthCallback != null) {
            biometricAuthCallback.onCancel();
        }
    }

    @Override
    public void onError() {
        dismissAllowingStateLoss();
        if (biometricAuthCallback != null) {
            biometricAuthCallback.onError();
        }
    }

}
