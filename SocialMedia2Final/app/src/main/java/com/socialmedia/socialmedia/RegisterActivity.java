package com.socialmedia.socialmedia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    //views
    EditText mEmailEt, mPasswordEt, mNameEt, mMobileEt, mConPasswordEt;
    Button mRegisterBtn;
    TextView mHaveAccountTv;
    Spinner mlocation, mtypeUserBox;

    String regex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{8,20}$";

    String [] neighborhoods= {"حدد الحي","الأندلس","الحمراء", "العزيزية", "مشرفة", "الرحاب"
            ,"النسيم", "بني مالك", "الورود", "الشرفية", "الرويس"
            ,"الروضة", "السلامة", "الزهرة", "الخالدية", "الشاطيء"
            ,"البوادي", "الربوة", "الفيصلية", "النزهة"
            ,"الثغر", "الجامعة", "الروابي", "السليمانية", "الفيحاء", "جامعة الملك عبد العزيز"
            ,"أبحر الجنوبية", "الأصالة", "البساتين", "المحمدية", "المرجان", "النعيم", "النهضة", "مطار الملك عبد العزيز"
            ,"أبحر الشمالية", "الأمواج", "البحيرات", "الخليج", "الزمرد", "الزهور", "الشراع", "الصواري", "الفردوس", "الفنار", "اللؤلو", "المنارات", "النجمة", "النور", "الياقوت", "حكومي"
            ,"الحرازات", "الرغامة", "المحاميد", "المنتزهات", "أم السلم", "كتانة"
            ,"البغدادية الشرقية", "البغدادية الغربية", "البلد", "السبيل", "الصحيفة", "العمارية", "الكندرة", "الميناء", "الهنداوية"
            ,"أبو جعالة", "الأثير", "الأجاويد", "الأمير عبد المجيد", "الأمير فواز الجنوبي", "الأمير فواز الشمالي", "البركة", "التضامن", "التعاون", "الجوهرة", "الرحمة", "الرهناء", "السنابل", "السهل", "الشفاء", "العدل", "العسيلة", "العليا", "الفضل", "الفضيلة", "الكرامة", "المرسلات", "المستقبل", "الهدى", "صناعي"
            ,"الأجواد", "التلال", "الحجاز", "الرواسي", "الريان", "السامر", "الكوثر", "العسلاء", "المنار", "المنتزه", "أم حبلين الشرقية", "أم حبلين الغربية", "بريمان"
            ,"البدور", "البشائر", "البيان", "التوفيق", "الحمدانية", "الرحمانية", "الرياض", "الصالحية", "الصفحة", "الصفوة", "الفروسية", "الفلاح", "المودة", "الندى", "الوداد", "الوفاء"
            ,"الجوهرة", "الخمرة", "الرابية", "الرمال", "الساحل", "السروات", "السرور", "الصناعية", "الضاحية", "القرينية","القوزين", "المحجر", "المرسى", "المسرة", "المليساء", "الموج", "الوادي", "الوزيرية", "قاعدة الملك فيصل"
            ,"الصفا", "المروة"
            ,"الثعالبة", "الفاروق", "القريات","النزلة الشرقية","النزلة اليمانية", "بترومين", "غليل","مدائن الفهد"
            ,"الحفنة", "القوس","النخيل","الشروق", "الواحة", "مريخ"};

    String [] UserType = {"حدد نوع المستخدم", "حساب لمؤسسة", "حساب شخصي"};


    //progressbar to display while registering user
    ProgressDialog progressDialog;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("إنشاء حساب");
        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //init
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        mNameEt = findViewById(R.id.NameEt);
        mMobileEt = findViewById(R.id.MobileEt);
        mConPasswordEt = findViewById(R.id.ConPasswordEt);
        mtypeUserBox = findViewById(R.id.typeUserBox);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mlocation = findViewById(R.id.location);
        mHaveAccountTv = findViewById(R.id.have_accountTv);

        ArrayAdapter <String> mtypeUserBoxAdapter = new ArrayAdapter<String>(RegisterActivity.this, android.R.layout.simple_spinner_item, UserType);
        mtypeUserBoxAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mtypeUserBox.setAdapter(mtypeUserBoxAdapter);
        mtypeUserBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String value = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(RegisterActivity.this, value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        ArrayAdapter<String> adapter=new ArrayAdapter<String>(RegisterActivity.this, android.R.layout.simple_spinner_item, neighborhoods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mlocation.setAdapter(adapter);

        mlocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String value = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(RegisterActivity.this, value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //In the onCreate() method, initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("يتم إنشاء الحساب...");





        //handle register btn click
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input email, password
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                String Name = mNameEt.getText().toString().trim();
                String Mobile = mMobileEt.getText().toString().trim();
                String ConPassword = mConPasswordEt.getText().toString().trim();
                String bio="";
                String UserT = mtypeUserBox.getSelectedItem().toString();
                String UserNeighborhood = mlocation.getSelectedItem().toString();


                //validate

                if (Name.isEmpty() || Name.length()<2){
                    mNameEt.setError("الإسم غير صالح");
                    mNameEt.setFocusable(true);
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //set error and focuss to email edittext
                    mEmailEt.setError("البريد الإلكتروني غير صالح");
                    mEmailEt.setFocusable(true);
                } else if (Mobile.isEmpty() || !Mobile.startsWith("05") || Mobile.length()!=10){
                    mMobileEt.setError("رقم الجوال غير صالح");
                    mMobileEt.setFocusable(true);
                } else if (!isValidPassword(password,regex)) {
                    //set error and focuss to password edittext
                    mPasswordEt.setError("-يجب أن يحتوي على 8 أحرف على الأقل و 20 حرفًا على الأكثر.\n" +
                            "-يجب أن يحتوي على رقم واحد على الأقل.\n" +
                            "-يجب أن تحتوي على أبجدية كبيرة واحدة على الأقل.\n" +
                            "-يجب أن يحتوي على أبجدية صغيرة واحدة على الأقل.\n" +
                            "-يجب أن يحتوي على حرف خاص واحد على الأقل.\n" +
                            "-يجب أن لا يحتوي على أي مساحة بيضاء.\n");
                    mPasswordEt.setFocusable(true);
                } else if (ConPassword.isEmpty() || !ConPassword.equals(password)){
                    mConPasswordEt.setError("كلمة المرور غير صحيحة");
                    mConPasswordEt.setFocusable(true);
                } else if (UserT.equals("حدد نوع المستخدم")) {

                    Toast.makeText(getApplicationContext(), "يجب تحديد نوع المستخدم", Toast.LENGTH_LONG).show();

                }else if (UserNeighborhood.equals("حدد الحي")) {
                    Toast.makeText(getApplicationContext(), "يجب تحديد الحي", Toast.LENGTH_LONG).show();

                }else {
                    register(email, password, Name, Mobile, bio, UserT, UserNeighborhood); //register the user

                }
            }
        });
        //handle login textview click listener
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });


    }


    private void register(String email, String password, String Name, String Mobile,String bio, String typeUser, String UserNeighborhood) {
        //email and password pattern is valid, show progress dialog and start registering user
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, dismiss dialog and start register activity
                            progressDialog.dismiss();

                            FirebaseUser user = mAuth.getCurrentUser();
                            //Get user email and uid from auth
                            String email = user.getEmail();
                            String uid = user.getUid();
                            //When user is registered store user info in firebase realtime database too
                            //using HashMap
                            HashMap<Object, String> hashMap = new HashMap<>();
                            //put info in hasmap
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", Name); //will add later (e.g. edit profile)
                            hashMap.put("onlineStatus", "online"); //will add later (e.g. edit profile)
                            hashMap.put("typingTo", "noOne"); //will add later (e.g. edit profile)
                            hashMap.put("phone", Mobile);
                            hashMap.put("image", ""); //will add later (e.g. edit profile)
                            hashMap.put("bio", bio); //will add later (e.g. edit profile)
                            hashMap.put("cover", ""); //will add later (e.g. edit profile)
                            if (typeUser.equals("حساب لمؤسسة")) {
                                hashMap.put("isBusinessOwner", "1");
                                hashMap.put("category", "9"); //will add later (e.g. edit profile)
                            }else if (typeUser.equals("حساب شخصي")){
                                hashMap.put("isBusinessOwner", "2");
                                hashMap.put("category", "10");
                            }
                            hashMap.put("UserNeighborhood", UserNeighborhood);

                            //firebase database isntance
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            //path to store user data named "Users"
                            DatabaseReference reference = database.getReference("Users");
                            //put data within hashmap in database
                            reference.child(uid).setValue(hashMap);
                            if (typeUser.equals("حساب لمؤسسة")) {
                                Toast.makeText(RegisterActivity.this, "تم تسجيل...\n" + user.getEmail(), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, BusinessOwnerDashboardActivity.class));
                                finish();
                            }else if (typeUser.equals("حساب شخصي")){
                                Toast.makeText(RegisterActivity.this, "تم تسجيل...\n" + user.getEmail(), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, IndividualDashboardActivity.class));
                                finish();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "فشل المصادقة.", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //error, dismiss progress dialog and get and show the error message
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // go previous activity
        return super.onSupportNavigateUp();
    }


    public static boolean isValidPassword(String password,String regex)
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}