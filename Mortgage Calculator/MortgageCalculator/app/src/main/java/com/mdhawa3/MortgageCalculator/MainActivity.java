package com.mdhawa3.MortgageCalculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnClickListener;

import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity {

    // currency and percent formatter objects
    private static final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance();
    private static final NumberFormat percentFormat =
            NumberFormat.getPercentInstance();

    private double housePrice=0.0;
    private double downPayment=0.0;
    private double annualRate=0.045;
    private double monthlyInstallment=0.0;
    private double totalPayment=0.0;
    private int years=0;
    private final int MONTHS_IN_A_YEAR = 12;
    private int months;

    private TextView monthlyInstallmentTextView; // shows calculated monthly Installment
    private TextView totalPaymentTextView; // shows calculated total payment amount
    //private TextView yearsTextView;

    private EditText housePriceEditText;
    private EditText downPaymentEditText;
    private EditText annualRateEditText;
    private EditText yearsEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        monthlyInstallmentTextView = (TextView) findViewById(R.id.monthlyInstallmentTextView);
        totalPaymentTextView = (TextView) findViewById(R.id.totalPaymentTextView);
        monthlyInstallmentTextView.setText(currencyFormat.format(0));
        totalPaymentTextView.setText(currencyFormat.format(0));

        Button btnCalculate = (Button) findViewById(R.id.btnCalculate);
        btnCalculate.setOnClickListener(mortgageCalculator);

        Button btnClear = (Button) findViewById((R.id.btnClear));
        btnClear.setOnClickListener(clearFields);

        housePriceEditText = (EditText) findViewById(R.id.housePriceEditText);
        downPaymentEditText = (EditText) findViewById(R.id.downPaymentEditText);
        annualRateEditText = (EditText) findViewById(R.id.annualRateEditText);
        yearsEditText = (EditText) findViewById(R.id.yearsEditText);
    }

    private void calculateMortgage()
    {
        try {
            housePrice = Double.parseDouble(housePriceEditText.getText().toString());
            downPayment = Double.parseDouble(downPaymentEditText.getText().toString());
            annualRate = Double.parseDouble(annualRateEditText.getText().toString())/100;
            years = Integer.parseInt(yearsEditText.getText().toString());
            double monthlyInterestRate = annualRate / MONTHS_IN_A_YEAR;
            int months = years * MONTHS_IN_A_YEAR;
            double loanAmount = housePrice - downPayment;
            monthlyInstallment = (loanAmount * monthlyInterestRate) / (1 - Math.pow(1 + monthlyInterestRate, -months));
            totalPayment = monthlyInstallment * months;

            monthlyInstallmentTextView.setText(currencyFormat.format(monthlyInstallment));
            totalPaymentTextView.setText(currencyFormat.format(totalPayment));
        }
        catch(NullPointerException npe)
        {
            monthlyInstallment=0.0;
            totalPayment=0.0;
        }
        catch(NumberFormatException nfe)
        {
            monthlyInstallment=0.0;
            totalPayment=0.0;
        }
    }

    public void resetFields()
    {
        housePriceEditText.setText("");
        downPaymentEditText.setText("");
        annualRateEditText.setText("");
        yearsEditText.setText("");
        monthlyInstallmentTextView.setText("");
        totalPaymentTextView.setText("");
    }

    private final OnClickListener mortgageCalculator = new OnClickListener() {
        @Override
        public void onClick(View v) {
            calculateMortgage();
        }
    };

    private final OnClickListener clearFields = new OnClickListener() {
        @Override
        public void onClick(View v) {
            resetFields();
        }
    };
}
