package com.simats.risenow.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.risenow.ui.components.AppBackground
import com.simats.risenow.ui.theme.*
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.android.billingclient.api.*

class BillingManager(private val context: Context, private val onSubscriptionSuccessCallback: () -> Unit) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"
        private const val SUBSCRIPTION_SKU = "univault_premium_subscription"
        private const val TEST_SUBSCRIPTION_SKU = "android.test.purchased"
    }

    private var billingClient: BillingClient
    var productDetails: ProductDetails? = null

    init {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        setupBillingClient()
    }

    private fun setupBillingClient() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing setup finished successfully")
                    querySubscriptionDetails()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }
            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing service disconnected")
            }
        })
    }

    private fun querySubscriptionDetails() {
        querySpecificProduct(SUBSCRIPTION_SKU, BillingClient.ProductType.SUBS) { success ->
            if (!success) {
                Log.w(TAG, "Real subscription product not found, trying test products...")
                querySpecificProduct(TEST_SUBSCRIPTION_SKU, BillingClient.ProductType.INAPP) { testSuccess ->
                    if (!testSuccess) {
                        Log.e(TAG, "Both real and test products failed")
                    }
                }
            }
        }
    }

    private fun querySpecificProduct(productId: String, productType: String, callback: (Boolean) -> Unit) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productType)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (productDetailsList.isNotEmpty()) {
                    productDetails = productDetailsList[0]
                    Log.d(TAG, "Product details retrieved successfully for: $productId")
                    callback(true)
                } else {
                    Log.e(TAG, "No product details found for: $productId")
                    callback(false)
                }
            } else {
                Log.e(TAG, "Failed to query product details for $productId: ${billingResult.debugMessage}")
                callback(false)
            }
        }
    }

    fun launchSubscriptionFlow(activity: Activity) {
        if (!billingClient.isReady) {
            Log.e(TAG, "Billing client is not ready")
            Toast.makeText(context, "Billing service not ready. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        if (productDetails != null) {
            val productDetailsParamsList = if (productDetails!!.productType == BillingClient.ProductType.SUBS) {
                val subscriptionOfferDetails = productDetails!!.subscriptionOfferDetails
                if (subscriptionOfferDetails.isNullOrEmpty()) {
                    Log.e(TAG, "No subscription offers available")
                    Toast.makeText(context, "No subscription offers available", Toast.LENGTH_SHORT).show()
                    return
                }

                val selectedOffer = subscriptionOfferDetails[0]
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails!!)
                        .setOfferToken(selectedOffer.offerToken)
                        .build()
                )
            } else {
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails!!)
                        .build()
                )
            }

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Billing flow launched successfully")
            } else {
                Log.e(TAG, "Failed to launch billing flow: ${billingResult.debugMessage}")
                Toast.makeText(context, "Failed to start subscription process: ${billingResult.debugMessage}", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.e(TAG, "No product details available")
            Toast.makeText(context, "Subscription not available. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { handlePurchase(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Toast.makeText(context, "Purchase canceled", Toast.LENGTH_SHORT).show()
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Toast.makeText(context, "You already have an active subscription", Toast.LENGTH_SHORT).show()
                onSubscriptionSuccessCallback()
            }
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
                Toast.makeText(context, "Subscription unavailable. Please download app from Play Store for testing.", Toast.LENGTH_LONG).show()
            }
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                Toast.makeText(context, "Configuration error. Check Play Console setup.", Toast.LENGTH_LONG).show()
            }
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                Toast.makeText(context, "Google Play services unavailable. Try again later.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(context, "Purchase failed (Code: ${billingResult.responseCode})", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        onSubscriptionSuccess()
                    }
                }
            } else {
                onSubscriptionSuccess()
            }
        }
    }

    private fun onSubscriptionSuccess() {
        Toast.makeText(context, "Subscription successful! Welcome to Premium!", Toast.LENGTH_LONG).show()
        val sharedPref = context.getSharedPreferences("subscription_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("is_premium_user", true)
            putLong("subscription_time", System.currentTimeMillis())
            apply()
        }
        onSubscriptionSuccessCallback()
    }

    fun onDestroy() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}

@Composable
fun SubscriptionScreen(onNavigateHome: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val billingManager = remember { BillingManager(context, onNavigateHome) }

    DisposableEffect(Unit) {
        onDispose {
            billingManager.onDestroy()
        }
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo Container
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                 Icon(
                    imageVector = Icons.Default.Diamond, // app icon approximation
                    contentDescription = null,
                    tint = PrimaryGradientStart,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("RiseNow", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Box(
                modifier = Modifier
                    .background(Color(0xFFFFD700).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("PREMIUM", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Unlock unlimited potential with premium features designed for your success",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Feature Cards
            FeatureCard(
                icon = Icons.Filled.FlashOn,
                title = "Ad-Free Experience",
                subtitle = "Pure learning, no interruptions",
                iconTint = Color(0xFFFFD700)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            FeatureCard(
                icon = Icons.Filled.Diamond,
                title = "Exclusive Tools",
                subtitle = "Advanced analytics, insights",
                iconTint = Color(0xFF6C63FF)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Pricing Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF6C63FF), Color(0xFF9D4EDD))
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Premium Plan", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Text("₹99 / month", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))
            Spacer(modifier = Modifier.height(32.dp))

            // Buttons
            Button(
                onClick = {
                    val activity = context as? Activity
                    if (activity != null) {
                        billingManager.launchSubscriptionFlow(activity)
                    } else {
                        Toast.makeText(context, "Unable to launch subscription from this context", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Start Premium", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Maybe later",
                color = Color.Gray,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { onNavigateHome() }
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Text(
                text = "Powered by SIMATS ENGINEERING",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FeatureCard(icon: ImageVector, title: String, subtitle: String, iconTint: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(subtitle, color = Color.Gray, fontSize = 14.sp)
                }
            }
            Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
        }
    }
}
