package com.example.coffeefirst.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.example.coffeefirst.R
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class QrDialogFragment : androidx.fragment.app.DialogFragment() {

    companion object {
        private const val ARG_QR_DATA = "qr_data"

        fun newInstance(qrData: String): QrDialogFragment {
            val args = Bundle()
            args.putString(ARG_QR_DATA, qrData)
            val fragment = QrDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val qrData = arguments?.getString(ARG_QR_DATA) ?: return
        val imageView = view.findViewById<ImageView>(R.id.largeQrImageView)

        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 800, 800)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Ошибка генерации QR", Toast.LENGTH_SHORT).show()
            dismiss()
        }
        view.setOnClickListener { dismiss() }
        imageView.setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}
