/** **********************************************************************
 *
 * Copyright 2018 Jochen Staerk
 *
 * Use is subject to license terms.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *********************************************************************** */
package org.mustangproject.ZUGFeRD;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.JAXBElement;
import org.mustangproject.ZUGFeRD.model.AmountType;
import org.mustangproject.ZUGFeRD.model.CodeType;
import org.mustangproject.ZUGFeRD.model.CountryIDType;
import org.mustangproject.ZUGFeRD.model.CreditorFinancialAccountType;
import org.mustangproject.ZUGFeRD.model.CreditorFinancialInstitutionType;
import org.mustangproject.ZUGFeRD.model.CrossIndustryDocumentType;
import org.mustangproject.ZUGFeRD.model.DateTimeType;
import org.mustangproject.ZUGFeRD.model.DateTimeTypeConstants;
import org.mustangproject.ZUGFeRD.model.DocumentCodeType;
import org.mustangproject.ZUGFeRD.model.DocumentCodeTypeConstants;
import org.mustangproject.ZUGFeRD.model.DocumentContextParameterType;
import org.mustangproject.ZUGFeRD.model.DocumentContextParameterTypeConstants;
import org.mustangproject.ZUGFeRD.model.DocumentLineDocumentType;
import org.mustangproject.ZUGFeRD.model.ExchangedDocumentContextType;
import org.mustangproject.ZUGFeRD.model.ExchangedDocumentType;
import org.mustangproject.ZUGFeRD.model.IDType;
import org.mustangproject.ZUGFeRD.model.IndicatorType;
import org.mustangproject.ZUGFeRD.model.LogisticsServiceChargeType;
import org.mustangproject.ZUGFeRD.model.NoteType;
import org.mustangproject.ZUGFeRD.model.NoteTypeConstants;
import org.mustangproject.ZUGFeRD.model.ObjectFactory;
import org.mustangproject.ZUGFeRD.model.PaymentMeansCodeType;
import org.mustangproject.ZUGFeRD.model.PaymentMeansCodeTypeConstants;
import org.mustangproject.ZUGFeRD.model.PercentType;
import org.mustangproject.ZUGFeRD.model.QuantityType;
import org.mustangproject.ZUGFeRD.model.SupplyChainEventType;
import org.mustangproject.ZUGFeRD.model.SupplyChainTradeAgreementType;
import org.mustangproject.ZUGFeRD.model.SupplyChainTradeDeliveryType;
import org.mustangproject.ZUGFeRD.model.SupplyChainTradeLineItemType;
import org.mustangproject.ZUGFeRD.model.SupplyChainTradeSettlementType;
import org.mustangproject.ZUGFeRD.model.SupplyChainTradeTransactionType;
import org.mustangproject.ZUGFeRD.model.TaxCategoryCodeType;
import org.mustangproject.ZUGFeRD.model.TaxCategoryCodeTypeConstants;
import org.mustangproject.ZUGFeRD.model.TaxRegistrationType;
import org.mustangproject.ZUGFeRD.model.TaxRegistrationTypeConstants;
import org.mustangproject.ZUGFeRD.model.TaxTypeCodeType;
import org.mustangproject.ZUGFeRD.model.TaxTypeCodeTypeConstants;
import org.mustangproject.ZUGFeRD.model.TextType;
import org.mustangproject.ZUGFeRD.model.TradeAddressType;
import org.mustangproject.ZUGFeRD.model.TradeAllowanceChargeType;
import org.mustangproject.ZUGFeRD.model.TradePartyType;
import org.mustangproject.ZUGFeRD.model.TradePaymentTermsType;
import org.mustangproject.ZUGFeRD.model.TradePriceType;
import org.mustangproject.ZUGFeRD.model.TradeProductType;
import org.mustangproject.ZUGFeRD.model.TradeSettlementMonetarySummationType;
import org.mustangproject.ZUGFeRD.model.TradeSettlementPaymentMeansType;
import org.mustangproject.ZUGFeRD.model.TradeTaxType;

class ZUGFeRDTransactionModelConverter {
    private static final SimpleDateFormat zugferdDateFormat = new SimpleDateFormat("yyyyMMdd");
    private static final ObjectFactory xmlFactory = new ObjectFactory();

    private final IZUGFeRDExportableTransaction trans;
    private final Totals totals;
    private boolean isTest;
    private String currency = "EUR";

    ZUGFeRDTransactionModelConverter(IZUGFeRDExportableTransaction trans) {
        this.trans = trans;
        totals = new Totals();
        this.currency = trans.getCurrency() != null ? trans.getCurrency(): currency;
    }


    JAXBElement<CrossIndustryDocumentType> convertToModel() {
        CrossIndustryDocumentType invoice = xmlFactory
            .createCrossIndustryDocumentType();

        invoice.setSpecifiedExchangedDocumentContext(this.getDocumentContext());
        invoice.setHeaderExchangedDocument(this.getDocument());
        invoice.setSpecifiedSupplyChainTradeTransaction(this
            .getTradeTransaction());

        return xmlFactory
            .createCrossIndustryDocument(invoice);
    }

    private ExchangedDocumentContextType getDocumentContext() {

        ExchangedDocumentContextType context = xmlFactory
            .createExchangedDocumentContextType();
        DocumentContextParameterType contextParameter = xmlFactory
            .createDocumentContextParameterType();
        IDType idType = xmlFactory.createIDType();
        idType.setValue(DocumentContextParameterTypeConstants.EXTENDED);
        contextParameter.setID(idType);
        context.getGuidelineSpecifiedDocumentContextParameter().add(
            contextParameter);

        IndicatorType testIndicator = xmlFactory.createIndicatorType();
        testIndicator.setIndicator(isTest);
        context.setTestIndicator(testIndicator);

        return context;
    }

    private ExchangedDocumentType getDocument() {

        ExchangedDocumentType document = xmlFactory
            .createExchangedDocumentType();

        IDType id = xmlFactory.createIDType();
        id.setValue(trans.getNumber());
        document.setID(id);

        DateTimeType issueDateTime = xmlFactory.createDateTimeType();
        DateTimeType.DateTimeString issueDateTimeString = xmlFactory
            .createDateTimeTypeDateTimeString();
        issueDateTimeString.setFormat(DateTimeTypeConstants.DATE);
        issueDateTimeString.setValue(zugferdDateFormat.format(trans
            .getIssueDate()));
        issueDateTime.setDateTimeString(issueDateTimeString);
        document.setIssueDateTime(issueDateTime);

        DocumentCodeType documentCodeType = xmlFactory.createDocumentCodeType();
        documentCodeType.setValue(DocumentCodeTypeConstants.INVOICE);
        document.setTypeCode(documentCodeType);

        TextType name = xmlFactory.createTextType();
        name.setValue("RECHNUNG");
        document.getName().add(name);

        if (trans.getOwnOrganisationFullPlaintextInfo() != null) {
            NoteType regularInfo = xmlFactory.createNoteType();
            CodeType regularInfoSubjectCode = xmlFactory.createCodeType();
            regularInfoSubjectCode.setValue(NoteTypeConstants.REGULARINFO);
            regularInfo.setSubjectCode(regularInfoSubjectCode);
            TextType regularInfoContent = xmlFactory.createTextType();
            regularInfoContent.setValue(trans
                .getOwnOrganisationFullPlaintextInfo());
            regularInfo.getContent().add(regularInfoContent);
            document.getIncludedNote().add(regularInfo);
        }

        if (trans.getReferenceNumber() != null && !new String().equals(trans.getReferenceNumber())){
            NoteType referenceInfo = xmlFactory.createNoteType();
            TextType referenceInfoContent = xmlFactory.createTextType();
            referenceInfoContent.setValue("Ursprungsbeleg: " + trans.getReferenceNumber());
            referenceInfo.getContent().add(referenceInfoContent);
            document.getIncludedNote().add(referenceInfo);
        }

        return document;
    }

    private SupplyChainTradeTransactionType getTradeTransaction() {

        SupplyChainTradeTransactionType transaction = xmlFactory
            .createSupplyChainTradeTransactionType();
        transaction.getApplicableSupplyChainTradeAgreement().add(
            this.getTradeAgreement());
        transaction.setApplicableSupplyChainTradeDelivery(this
            .getTradeDelivery());
        transaction.setApplicableSupplyChainTradeSettlement(this
            .getTradeSettlement());
        transaction.getIncludedSupplyChainTradeLineItem().addAll(
            this.getLineItems());

        return transaction;
    }

    private SupplyChainTradeAgreementType getTradeAgreement() {

        SupplyChainTradeAgreementType tradeAgreement = xmlFactory
            .createSupplyChainTradeAgreementType();

        tradeAgreement.setBuyerTradeParty(this.getBuyer());
        tradeAgreement.setSellerTradeParty(this.getSeller());

        return tradeAgreement;
    }

    private TradePartyType getBuyer() {

        TradePartyType buyerTradeParty = xmlFactory.createTradePartyType();
        TextType buyerName = xmlFactory.createTextType();
        buyerName.setValue(trans.getRecipient().getName());
        buyerTradeParty.setName(buyerName);

        TradeAddressType buyerAddressType = xmlFactory.createTradeAddressType();
        TextType buyerCityName = xmlFactory.createTextType();
        buyerCityName.setValue(trans.getRecipient().getLocation());
        buyerAddressType.setCityName(buyerCityName);

        CountryIDType buyerCountryId = xmlFactory.createCountryIDType();
        buyerCountryId.setValue(trans.getRecipient().getCountry());
        buyerAddressType.setCountryID(buyerCountryId);

        TextType buyerAddress = xmlFactory.createTextType();
        buyerAddress.setValue(trans.getRecipient().getStreet());
        buyerAddressType.setLineOne(buyerAddress);

        CodeType buyerPostcode = xmlFactory.createCodeType();
        buyerPostcode.setValue(trans.getRecipient().getZIP());
        buyerAddressType.getPostcodeCode().add(buyerPostcode);

        buyerTradeParty.setPostalTradeAddress(buyerAddressType);

        // Ust-ID
        TaxRegistrationType buyerTaxRegistration = xmlFactory
            .createTaxRegistrationType();
        IDType buyerUstId = xmlFactory.createIDType();
        buyerUstId.setValue(trans.getRecipient().getVATID());
        buyerUstId.setSchemeID(TaxRegistrationTypeConstants.USTID);
        buyerTaxRegistration.setID(buyerUstId);
        buyerTradeParty.getSpecifiedTaxRegistration().add(buyerTaxRegistration);

        return buyerTradeParty;
    }

    private TradePartyType getSeller() {

        TradePartyType sellerTradeParty = xmlFactory.createTradePartyType();
        TextType sellerName = xmlFactory.createTextType();
        sellerName.setValue(trans.getOwnOrganisationName());
        sellerTradeParty.setName(sellerName);

        TradeAddressType sellerAddressType = xmlFactory
            .createTradeAddressType();
        TextType sellerCityName = xmlFactory.createTextType();
        sellerCityName.setValue(trans.getOwnLocation());
        sellerAddressType.setCityName(sellerCityName);

        CountryIDType sellerCountryId = xmlFactory.createCountryIDType();
        sellerCountryId.setValue(trans.getOwnCountry());
        sellerAddressType.setCountryID(sellerCountryId);

        TextType sellerAddress = xmlFactory.createTextType();
        sellerAddress.setValue(trans.getOwnStreet());
        sellerAddressType.setLineOne(sellerAddress);

        CodeType sellerPostcode = xmlFactory.createCodeType();
        sellerPostcode.setValue(trans.getOwnZIP());
        sellerAddressType.getPostcodeCode().add(sellerPostcode);

        sellerTradeParty.setPostalTradeAddress(sellerAddressType);

        // Steuernummer
        TaxRegistrationType sellerTaxRegistration = xmlFactory
            .createTaxRegistrationType();
        IDType sellerTaxId = xmlFactory.createIDType();
        sellerTaxId.setValue(trans.getOwnTaxID());
        sellerTaxId.setSchemeID(TaxRegistrationTypeConstants.TAXID);
        sellerTaxRegistration.setID(sellerTaxId);
        sellerTradeParty.getSpecifiedTaxRegistration().add(
            sellerTaxRegistration);

        // Ust-ID
        sellerTaxRegistration = xmlFactory.createTaxRegistrationType();
        IDType sellerUstId = xmlFactory.createIDType();
        sellerUstId.setValue(trans.getOwnVATID());
        sellerUstId.setSchemeID(TaxRegistrationTypeConstants.USTID);
        sellerTaxRegistration.setID(sellerUstId);
        sellerTradeParty.getSpecifiedTaxRegistration().add(
            sellerTaxRegistration);

        return sellerTradeParty;
    }

    private SupplyChainTradeDeliveryType getTradeDelivery() {

        SupplyChainTradeDeliveryType tradeDelivery = xmlFactory
            .createSupplyChainTradeDeliveryType();
        SupplyChainEventType deliveryEvent = xmlFactory
            .createSupplyChainEventType();
        DateTimeType deliveryDate = xmlFactory.createDateTimeType();
        DateTimeType.DateTimeString deliveryDateString = xmlFactory
            .createDateTimeTypeDateTimeString();
        deliveryDateString.setFormat(DateTimeTypeConstants.DATE);
        deliveryDateString.setValue(zugferdDateFormat.format(trans
            .getDeliveryDate()));
        deliveryDate.setDateTimeString(deliveryDateString);
        deliveryEvent.getOccurrenceDateTime().add(deliveryDate);
        tradeDelivery.getActualDeliverySupplyChainEvent().add(deliveryEvent);

        return tradeDelivery;
    }

    private SupplyChainTradeSettlementType getTradeSettlement() {
        SupplyChainTradeSettlementType tradeSettlement = xmlFactory
            .createSupplyChainTradeSettlementType();

        TextType paymentReference = xmlFactory.createTextType();
        paymentReference.setValue(trans.getNumber());
        tradeSettlement.getPaymentReference().add(paymentReference);

        CodeType currencyCode = xmlFactory.createCodeType();
        currencyCode.setValue(currency);
        tradeSettlement.setInvoiceCurrencyCode(currencyCode);

        /*tradeSettlement.getSpecifiedTradeSettlementPaymentMeans().add(
            this.getPaymentData());*/
        tradeSettlement.getApplicableTradeTax().addAll(this.getTradeTax());
        tradeSettlement.getSpecifiedTradePaymentTerms().addAll(
            this.getPaymentTerms());
        if (trans.getZFAllowances() != null) {
            tradeSettlement.getSpecifiedTradeAllowanceCharge().addAll(
                this.getHeaderAllowances());
        }
        if (trans.getZFLogisticsServiceCharges() != null) {
            tradeSettlement.getSpecifiedLogisticsServiceCharge().addAll(
                this.getHeaderLogisticsServiceCharges());
        }
        if (trans.getZFCharges() != null) {
            tradeSettlement.getSpecifiedTradeAllowanceCharge().addAll(
                this.getHeaderCharges());
        }

        tradeSettlement.setSpecifiedTradeSettlementMonetarySummation(this
            .getMonetarySummation());

        return tradeSettlement;
    }

    private TradeSettlementPaymentMeansType getPaymentData() {
        TradeSettlementPaymentMeansType paymentData = xmlFactory
            .createTradeSettlementPaymentMeansType();
        PaymentMeansCodeType paymentDataType = xmlFactory
            .createPaymentMeansCodeType();
        paymentDataType.setValue(PaymentMeansCodeTypeConstants.BANKACCOUNT);
        paymentData.setTypeCode(paymentDataType);

        TextType paymentInfo = xmlFactory.createTextType();
        String paymentInfoText = trans.getOwnPaymentInfoText();
        if (paymentInfoText == null) {
            paymentInfoText = "";
        }
        paymentInfo.setValue(paymentInfoText);
        paymentData.getInformation().add(paymentInfo);

        CreditorFinancialAccountType bankAccount = xmlFactory
            .createCreditorFinancialAccountType();
        IDType iban = xmlFactory.createIDType();
        iban.setValue(trans.getOwnIBAN());
        bankAccount.setIBANID(iban);
        paymentData.setPayeePartyCreditorFinancialAccount(bankAccount);

        CreditorFinancialInstitutionType bankData = xmlFactory
            .createCreditorFinancialInstitutionType();
        IDType bicId = xmlFactory.createIDType();
        bicId.setValue(trans.getOwnBIC());
        bankData.setBICID(bicId);
        TextType bankName = xmlFactory.createTextType();
        bankName.setValue(trans.getOwnBankName());
        bankData.setName(bankName);
        paymentData.setPayeeSpecifiedCreditorFinancialInstitution(bankData);
        return paymentData;
    }

    private Collection<TradeTaxType> getTradeTax() {
        List<TradeTaxType> tradeTaxTypes = new ArrayList<TradeTaxType>();

        HashMap<BigDecimal, VATAmount> VATPercentAmountMap = this
            .getVATPercentAmountMap();
        for (BigDecimal currentTaxPercent : VATPercentAmountMap.keySet()) {

            TradeTaxType tradeTax = xmlFactory.createTradeTaxType();
            TaxTypeCodeType taxTypeCode = xmlFactory.createTaxTypeCodeType();
            taxTypeCode.setValue(TaxTypeCodeTypeConstants.SALESTAX);
            tradeTax.setTypeCode(taxTypeCode);

            TaxCategoryCodeType taxCategoryCode = xmlFactory
                .createTaxCategoryCodeType();
            taxCategoryCode.setValue(TaxCategoryCodeTypeConstants.STANDARDRATE);
            tradeTax.setCategoryCode(taxCategoryCode);

            VATAmount amount = VATPercentAmountMap.get(currentTaxPercent);

            PercentType taxPercent = xmlFactory.createPercentType();
            taxPercent.setValue(vatFormat(currentTaxPercent));
            tradeTax.setApplicablePercent(taxPercent);

            AmountType calculatedTaxAmount = xmlFactory.createAmountType();
            calculatedTaxAmount.setCurrencyID(currency);
            calculatedTaxAmount
                .setValue(currencyFormat(amount.getCalculated()));
            tradeTax.getCalculatedAmount().add(calculatedTaxAmount);

            AmountType basisTaxAmount = xmlFactory.createAmountType();
            basisTaxAmount.setCurrencyID(currency);
            basisTaxAmount.setValue(currencyFormat(amount.getBasis()));
            tradeTax.getBasisAmount().add(basisTaxAmount);

            tradeTaxTypes.add(tradeTax);
        }

        return tradeTaxTypes;
    }

    private Collection<TradeAllowanceChargeType> getHeaderAllowances() {
        List<TradeAllowanceChargeType> headerAllowances = new ArrayList<TradeAllowanceChargeType>();

        for (IZUGFeRDAllowanceCharge iAllowance : trans.getZFAllowances()) {

            TradeAllowanceChargeType allowance = xmlFactory
                .createTradeAllowanceChargeType();

            IndicatorType chargeIndicator = xmlFactory.createIndicatorType();
            chargeIndicator.setIndicator(false);
            allowance.setChargeIndicator(chargeIndicator);

            AmountType actualAmount = xmlFactory.createAmountType();
            actualAmount.setCurrencyID(currency);
            actualAmount.setValue(currencyFormat(iAllowance.getTotalAmount()));
            allowance.getActualAmount().add(actualAmount);

            TextType reason = xmlFactory.createTextType();
            reason.setValue(iAllowance.getReason());
            allowance.setReason(reason);

            TradeTaxType tradeTax = xmlFactory.createTradeTaxType();

            PercentType vatPercent = xmlFactory.createPercentType();
            vatPercent.setValue(currencyFormat(iAllowance.getTaxPercent()));
            tradeTax.setApplicablePercent(vatPercent);

			/*
			 * Only in extended AmountType basisAmount =
			 * xmlFactory.createAmountType();
			 * basisAmount.setCurrencyID(trans.getInvoiceCurrency());
			 * basisAmount.setValue(amount.getBasis());
			 * allowance.setBasisAmount(basisAmount);
			 */
            TaxCategoryCodeType taxType = xmlFactory
                .createTaxCategoryCodeType();
            taxType.setValue(TaxCategoryCodeTypeConstants.STANDARDRATE);
            tradeTax.setCategoryCode(taxType);

            TaxTypeCodeType taxCode = xmlFactory.createTaxTypeCodeType();
            taxCode.setValue(TaxTypeCodeTypeConstants.SALESTAX);
            tradeTax.setTypeCode(taxCode);

            allowance.getCategoryTradeTax().add(tradeTax);
            headerAllowances.add(allowance);

        }

        return headerAllowances;
    }

    private Collection<TradeAllowanceChargeType> getHeaderCharges() {
        List<TradeAllowanceChargeType> headerCharges = new ArrayList<TradeAllowanceChargeType>();

        for (IZUGFeRDAllowanceCharge iCharge : trans.getZFCharges()) {

            TradeAllowanceChargeType charge = xmlFactory
                .createTradeAllowanceChargeType();

            IndicatorType chargeIndicator = xmlFactory.createIndicatorType();
            chargeIndicator.setIndicator(true);
            charge.setChargeIndicator(chargeIndicator);

            AmountType actualAmount = xmlFactory.createAmountType();
            actualAmount.setCurrencyID(currency);
            actualAmount.setValue(currencyFormat(iCharge.getTotalAmount()));
            charge.getActualAmount().add(actualAmount);

            TextType reason = xmlFactory.createTextType();
            reason.setValue(iCharge.getReason());
            charge.setReason(reason);

            TradeTaxType tradeTax = xmlFactory.createTradeTaxType();

            PercentType vatPercent = xmlFactory.createPercentType();
            vatPercent.setValue(currencyFormat(iCharge.getTaxPercent()));
            tradeTax.setApplicablePercent(vatPercent);

			/*
			 * Only in extended AmountType basisAmount =
			 * xmlFactory.createAmountType();
			 * basisAmount.setCurrencyID(trans.getInvoiceCurrency());
			 * basisAmount.setValue(amount.getBasis());
			 * allowance.setBasisAmount(basisAmount);
			 */
            TaxCategoryCodeType taxType = xmlFactory
                .createTaxCategoryCodeType();
            taxType.setValue(TaxCategoryCodeTypeConstants.STANDARDRATE);
            tradeTax.setCategoryCode(taxType);

            TaxTypeCodeType taxCode = xmlFactory.createTaxTypeCodeType();
            taxCode.setValue(TaxTypeCodeTypeConstants.SALESTAX);
            tradeTax.setTypeCode(taxCode);

            charge.getCategoryTradeTax().add(tradeTax);
            headerCharges.add(charge);

        }

        return headerCharges;
    }

    private Collection<LogisticsServiceChargeType> getHeaderLogisticsServiceCharges() {
        List<LogisticsServiceChargeType> headerServiceCharge = new ArrayList<LogisticsServiceChargeType>();

        for (IZUGFeRDAllowanceCharge iServiceCharge : trans
            .getZFLogisticsServiceCharges()) {

            LogisticsServiceChargeType serviceCharge = xmlFactory
                .createLogisticsServiceChargeType();

            AmountType actualAmount = xmlFactory.createAmountType();
            actualAmount.setCurrencyID(currency);
            actualAmount.setValue(currencyFormat(iServiceCharge
                .getTotalAmount()));
            serviceCharge.getAppliedAmount().add(actualAmount);

            TextType reason = xmlFactory.createTextType();
            reason.setValue(iServiceCharge.getReason());
            serviceCharge.getDescription().add(reason);

            TradeTaxType tradeTax = xmlFactory.createTradeTaxType();

            PercentType vatPercent = xmlFactory.createPercentType();
            vatPercent.setValue(currencyFormat(iServiceCharge.getTaxPercent()));
            tradeTax.setApplicablePercent(vatPercent);

			/*
			 * Only in extended AmountType basisAmount =
			 * xmlFactory.createAmountType();
			 * basisAmount.setCurrencyID(trans.getInvoiceCurrency());
			 * basisAmount.setValue(amount.getBasis());
			 * allowance.setBasisAmount(basisAmount);
			 */
            TaxCategoryCodeType taxType = xmlFactory
                .createTaxCategoryCodeType();
            taxType.setValue(TaxCategoryCodeTypeConstants.STANDARDRATE);
            tradeTax.setCategoryCode(taxType);

            TaxTypeCodeType taxCode = xmlFactory.createTaxTypeCodeType();
            taxCode.setValue(TaxTypeCodeTypeConstants.SALESTAX);
            tradeTax.setTypeCode(taxCode);

            serviceCharge.getAppliedTradeTax().add(tradeTax);
            headerServiceCharge.add(serviceCharge);

        }

        return headerServiceCharge;
    }

    private Collection<TradePaymentTermsType> getPaymentTerms() {
        List<TradePaymentTermsType> paymentTerms = new ArrayList<TradePaymentTermsType>();

        TradePaymentTermsType paymentTerm = xmlFactory
            .createTradePaymentTermsType();
        DateTimeType dueDate = xmlFactory.createDateTimeType();
        DateTimeType.DateTimeString dueDateString = xmlFactory
            .createDateTimeTypeDateTimeString();
        dueDateString.setFormat(DateTimeTypeConstants.DATE);
        dueDateString.setValue(zugferdDateFormat.format(trans.getDueDate()));
        dueDate.setDateTimeString(dueDateString);
        paymentTerm.setDueDateDateTime(dueDate);

        TextType paymentTermDescr = xmlFactory.createTextType();

        String paymentTermDescription = trans.getPaymentTermDescription();
        if (paymentTermDescription == null) {
            paymentTermDescription = "";
        }
        paymentTermDescr.setValue(paymentTermDescription);
        paymentTerm.getDescription().add(paymentTermDescr);

        paymentTerms.add(paymentTerm);

        return paymentTerms;
    }

    private TradeSettlementMonetarySummationType getMonetarySummation() {
        TradeSettlementMonetarySummationType monetarySummation = xmlFactory
            .createTradeSettlementMonetarySummationType();

        // AllowanceTotalAmount = sum of all allowances
        AmountType allowanceTotalAmount = xmlFactory.createAmountType();
        allowanceTotalAmount.setCurrencyID(currency);
        if (trans.getZFAllowances() != null) {
            BigDecimal totalHeaderAllowance = BigDecimal.ZERO;
            for (IZUGFeRDAllowanceCharge headerAllowance : trans
                .getZFAllowances()) {
                totalHeaderAllowance = headerAllowance.getTotalAmount().add(
                    totalHeaderAllowance);
            }
            allowanceTotalAmount.setValue(currencyFormat(totalHeaderAllowance));
        } else {
            allowanceTotalAmount.setValue(currencyFormat(BigDecimal.ZERO));
        }
        monetarySummation.getAllowanceTotalAmount().add(allowanceTotalAmount);

        // ChargeTotalAmount = sum of all Logistic service charges + normal
        // charges
        BigDecimal totalCharge = BigDecimal.ZERO;
        AmountType totalChargeAmount = xmlFactory.createAmountType();
        totalChargeAmount.setCurrencyID(currency);
        if (trans.getZFLogisticsServiceCharges() != null) {
            for (IZUGFeRDAllowanceCharge logisticsServiceCharge : trans
                .getZFLogisticsServiceCharges()) {
                totalCharge = logisticsServiceCharge.getTotalAmount().add(
                    totalCharge);
            }
        }
        if (trans.getZFCharges() != null) {
            for (IZUGFeRDAllowanceCharge charge : trans.getZFCharges()) {
                totalCharge = charge.getTotalAmount().add(totalCharge);
            }
        }

        totalChargeAmount.setValue(currencyFormat(totalCharge));

        monetarySummation.getChargeTotalAmount().add(totalChargeAmount);

		/*
		 * AmountType chargeTotalAmount = xmlFactory.createAmountType();
		 * chargeTotalAmount.setCurrencyID(trans.getInvoiceCurrency());
		 * chargeTotalAmount.setValue(currencyFormat(BigDecimal.ZERO));
		 * monetarySummation.getChargeTotalAmount().add(chargeTotalAmount);
		 */

        AmountType lineTotalAmount = xmlFactory.createAmountType();
        lineTotalAmount.setCurrencyID(currency);
        lineTotalAmount.setValue(currencyFormat(totals.getLineTotal()));
        monetarySummation.getLineTotalAmount().add(lineTotalAmount);

        AmountType taxBasisTotalAmount = xmlFactory.createAmountType();
        taxBasisTotalAmount.setCurrencyID(currency);
        taxBasisTotalAmount.setValue(currencyFormat(totals.getTotalNet()));
        monetarySummation.getTaxBasisTotalAmount().add(taxBasisTotalAmount);

        AmountType taxTotalAmount = xmlFactory.createAmountType();
        taxTotalAmount.setCurrencyID(currency);
        taxTotalAmount.setValue(currencyFormat(totals.getTaxTotal()));
        monetarySummation.getTaxTotalAmount().add(taxTotalAmount);

        AmountType grandTotalAmount = xmlFactory.createAmountType();
        grandTotalAmount.setCurrencyID(currency);
        grandTotalAmount.setValue(currencyFormat(totals.getTotalGross()));
        monetarySummation.getGrandTotalAmount().add(grandTotalAmount);

        AmountType duePayableAmount = xmlFactory.createAmountType();
        duePayableAmount.setCurrencyID(currency);
        duePayableAmount.setValue(currencyFormat(totals.getTotalGross()));
        monetarySummation.getDuePayableAmount().add(duePayableAmount);

        return monetarySummation;
    }

    private Collection<SupplyChainTradeLineItemType> getLineItems() {

        ArrayList<SupplyChainTradeLineItemType> lineItems = new ArrayList<SupplyChainTradeLineItemType>();
        int lineID = 0;
        for (IZUGFeRDExportableItem currentItem : trans.getZFItems()) {
            lineID++;
            LineCalc lc = new LineCalc(currentItem);
            SupplyChainTradeLineItemType lineItem = xmlFactory
                .createSupplyChainTradeLineItemType();

            DocumentLineDocumentType lineDocument = xmlFactory
                .createDocumentLineDocumentType();
            IDType lineNumber = xmlFactory.createIDType();
            lineNumber.setValue(Integer.toString(lineID));
            lineDocument.setLineID(lineNumber);
            lineItem.setAssociatedDocumentLineDocument(lineDocument);

            SupplyChainTradeAgreementType tradeAgreement = xmlFactory
                .createSupplyChainTradeAgreementType();
            TradePriceType grossTradePrice = xmlFactory.createTradePriceType();
            QuantityType grossQuantity = xmlFactory.createQuantityType();
            grossQuantity.setUnitCode(currentItem.getProduct().getUnit());
            grossQuantity.setValue(quantityFormat(BigDecimal.ONE));
            grossTradePrice.setBasisQuantity(grossQuantity);

            AmountType grossChargeAmount = xmlFactory.createAmountType();
            grossChargeAmount.setCurrencyID(currency);
            grossChargeAmount.setValue(priceFormat(currentItem.getPrice()));
            grossTradePrice.getChargeAmount().add(grossChargeAmount);
            tradeAgreement.getGrossPriceProductTradePrice()
                .add(grossTradePrice);

            if (currentItem.getItemAllowances() != null) {
                for (IZUGFeRDAllowanceCharge itemAllowance : currentItem
                    .getItemAllowances()) {
                    TradeAllowanceChargeType eItemAllowance = xmlFactory
                        .createTradeAllowanceChargeType();
                    IndicatorType chargeIndicator = xmlFactory
                        .createIndicatorType();
                    chargeIndicator.setIndicator(false);
                    eItemAllowance.setChargeIndicator(chargeIndicator);
                    AmountType actualAmount = xmlFactory.createAmountType();
                    actualAmount.setCurrencyID(currency);
                    actualAmount.setValue(priceFormat(itemAllowance
                        .getTotalAmount().divide(currentItem.getQuantity(),
                            4, BigDecimal.ROUND_HALF_UP)));
                    eItemAllowance.getActualAmount().add(actualAmount);
                    TextType reason = xmlFactory.createTextType();
                    reason.setValue(itemAllowance.getReason());
                    eItemAllowance.setReason(reason);
                    grossTradePrice.getAppliedTradeAllowanceCharge().add(
                        eItemAllowance);
                }
            }

            if (currentItem.getItemCharges() != null) {
                for (IZUGFeRDAllowanceCharge itemCharge : currentItem
                    .getItemCharges()) {
                    TradeAllowanceChargeType eItemCharge = xmlFactory
                        .createTradeAllowanceChargeType();
                    AmountType actualAmount = xmlFactory.createAmountType();
                    actualAmount.setCurrencyID(currency);
                    actualAmount.setValue(priceFormat(itemCharge
                        .getTotalAmount().divide(currentItem.getQuantity(),
                            4, BigDecimal.ROUND_HALF_UP)));
                    eItemCharge.getActualAmount().add(actualAmount);
                    TextType reason = xmlFactory.createTextType();
                    reason.setValue(itemCharge.getReason());
                    eItemCharge.setReason(reason);
                    IndicatorType chargeIndicator = xmlFactory
                        .createIndicatorType();
                    chargeIndicator.setIndicator(true);
                    eItemCharge.setChargeIndicator(chargeIndicator);
                    grossTradePrice.getAppliedTradeAllowanceCharge().add(
                        eItemCharge);
                }
            }

            TradePriceType netTradePrice = xmlFactory.createTradePriceType();
            QuantityType netQuantity = xmlFactory.createQuantityType();
            netQuantity.setUnitCode(currentItem.getProduct().getUnit());
            netQuantity.setValue(quantityFormat(BigDecimal.ONE));
            netTradePrice.setBasisQuantity(netQuantity);

            AmountType netChargeAmount = xmlFactory.createAmountType();
            netChargeAmount.setCurrencyID(currency);
            netChargeAmount.setValue(priceFormat(lc.getItemNetAmount()));
            netTradePrice.getChargeAmount().add(netChargeAmount);
            tradeAgreement.getNetPriceProductTradePrice().add(netTradePrice);

            lineItem.setSpecifiedSupplyChainTradeAgreement(tradeAgreement);

            SupplyChainTradeDeliveryType tradeDelivery = xmlFactory
                .createSupplyChainTradeDeliveryType();
            QuantityType billedQuantity = xmlFactory.createQuantityType();
            billedQuantity.setUnitCode(currentItem.getProduct().getUnit());
            billedQuantity.setValue(quantityFormat(currentItem.getQuantity()));
            tradeDelivery.setBilledQuantity(billedQuantity);
            lineItem.setSpecifiedSupplyChainTradeDelivery(tradeDelivery);

            SupplyChainTradeSettlementType tradeSettlement = xmlFactory
                .createSupplyChainTradeSettlementType();
            TradeTaxType tradeTax = xmlFactory.createTradeTaxType();

            TaxCategoryCodeType taxCategoryCode = xmlFactory
                .createTaxCategoryCodeType();
            taxCategoryCode.setValue(TaxCategoryCodeTypeConstants.STANDARDRATE);
            tradeTax.setCategoryCode(taxCategoryCode);

            TaxTypeCodeType taxCode = xmlFactory.createTaxTypeCodeType();
            taxCode.setValue(TaxTypeCodeTypeConstants.SALESTAX);
            tradeTax.setTypeCode(taxCode);

            PercentType taxPercent = xmlFactory.createPercentType();
            taxPercent.setValue(vatFormat(currentItem.getProduct()
                .getVATPercent()));
            tradeTax.setApplicablePercent(taxPercent);
            tradeSettlement.getApplicableTradeTax().add(tradeTax);

            TradeSettlementMonetarySummationType monetarySummation = xmlFactory
                .createTradeSettlementMonetarySummationType();
            AmountType itemAmount = xmlFactory.createAmountType();
            itemAmount.setCurrencyID(currency);
            itemAmount.setValue(currencyFormat(lc.getItemTotalNetAmount()));
            monetarySummation.getLineTotalAmount().add(itemAmount);
            tradeSettlement
                .setSpecifiedTradeSettlementMonetarySummation(monetarySummation);

            lineItem.setSpecifiedSupplyChainTradeSettlement(tradeSettlement);

            TradeProductType tradeProduct = xmlFactory.createTradeProductType();
            TextType productName = xmlFactory.createTextType();
            productName.setValue(currentItem.getProduct().getName());
            tradeProduct.getName().add(productName);

            TextType productDescription = xmlFactory.createTextType();
            productDescription.setValue(currentItem.getProduct()
                .getDescription());
            tradeProduct.getDescription().add(productDescription);
            lineItem.setSpecifiedTradeProduct(tradeProduct);

            lineItems.add(lineItem);
        }

        return lineItems;
    }

    private BigDecimal vatFormat(BigDecimal value) {
        return nDigitFormat(value, 2);
    }

    private BigDecimal currencyFormat(BigDecimal value) {
        return nDigitFormat(value, 2);
    }

    private BigDecimal priceFormat(BigDecimal value) {
        return nDigitFormat(value, 4);
    }

    private BigDecimal quantityFormat(BigDecimal value) {
        return nDigitFormat(value, 4);
    }

    private BigDecimal nDigitFormat(BigDecimal value, int scale) {
		/*
		 * I needed 123,45, locale independent.I tried
		 * NumberFormat.getCurrencyInstance().format( 12345.6789 ); but that is
		 * locale specific.I also tried DecimalFormat df = new DecimalFormat(
		 * "0,00" ); df.setDecimalSeparatorAlwaysShown(true);
		 * df.setGroupingUsed(false); DecimalFormatSymbols symbols = new
		 * DecimalFormatSymbols(); symbols.setDecimalSeparator(',');
		 * symbols.setGroupingSeparator(' ');
		 * df.setDecimalFormatSymbols(symbols);
		 *
		 * but that would not switch off grouping. Although I liked very much
		 * the (incomplete) "BNF diagram" in
		 * http://docs.oracle.com/javase/tutorial/i18n/format/decimalFormat.html
		 * in the end I decided to calculate myself and take eur+sparator+cents
		 *
		 * This function will cut off, i.e. floor() subcent values Tests:
		 * System.err.println(utils.currencyFormat(new BigDecimal(0),
		 * ".")+"\n"+utils.currencyFormat(new BigDecimal("-1.10"),
		 * ",")+"\n"+utils.currencyFormat(new BigDecimal("-1.1"),
		 * ",")+"\n"+utils.currencyFormat(new BigDecimal("-1.01"),
		 * ",")+"\n"+utils.currencyFormat(new BigDecimal("20000123.3489"),
		 * ",")+"\n"+utils.currencyFormat(new BigDecimal("20000123.3419"),
		 * ",")+"\n"+utils.currencyFormat(new BigDecimal("12"), ","));
		 *
		 * results 0.00 -1,10 -1,10 -1,01 20000123,34 20000123,34 12,00
		 */
        value = value.setScale(scale, BigDecimal.ROUND_HALF_UP); // first, round
        // so that
        // e.g.
        // 1.189999999999999946709294817992486059665679931640625
        // becomes
        // 1.19
        char[] repeat = new char[scale];
        Arrays.fill(repeat, '0');

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat dec = new DecimalFormat("0." + new String(repeat),
            otherSymbols);
        return new BigDecimal(dec.format(value));

    }

    /**
     * which taxes have been used with which amounts in this transaction, empty
     * for no taxes, or e.g. 19=>190 and 7=>14 if 1000 Eur were applicable to
     * 19% VAT (=>190 EUR VAT) and 200 EUR were applicable to 7% (=>14 EUR VAT)
     * 190 Eur
     *
     * @return HashMap<BigDecimal, VATAmount> which taxes have been used with which amounts
     *
     */
    private HashMap<BigDecimal, VATAmount> getVATPercentAmountMap() {
        return getVATPercentAmountMap(false);
    }

    private HashMap<BigDecimal, VATAmount> getVATPercentAmountMap(
        Boolean itemOnly) {
        HashMap<BigDecimal, VATAmount> hm = new HashMap<BigDecimal, VATAmount>();

        for (IZUGFeRDExportableItem currentItem : trans.getZFItems()) {
            BigDecimal percent = currentItem.getProduct().getVATPercent();
            LineCalc lc = new LineCalc(currentItem);
            VATAmount itemVATAmount = new VATAmount(lc.getItemTotalNetAmount(),
                lc.getItemTotalVATAmount());
            VATAmount current = hm.get(percent);
            if (current == null) {
                hm.put(percent, itemVATAmount);
            } else {
                hm.put(percent, current.add(itemVATAmount));
            }
        }
        if (itemOnly) {
            return hm;
        }
        if (trans.getZFAllowances() != null) {
            for (IZUGFeRDAllowanceCharge headerAllowance : trans
                .getZFAllowances()) {
                BigDecimal percent = headerAllowance.getTaxPercent();
                VATAmount itemVATAmount = new VATAmount(
                    headerAllowance.getTotalAmount(), headerAllowance
                    .getTotalAmount().multiply(percent)
                    .divide(new BigDecimal(100)));
                VATAmount current = hm.get(percent);
                if (current == null) {
                    hm.put(percent, itemVATAmount);
                } else {
                    hm.put(percent, current.subtract(itemVATAmount));
                }
            }
        }

        if (trans.getZFLogisticsServiceCharges() != null) {
            for (IZUGFeRDAllowanceCharge logisticsServiceCharge : trans
                .getZFLogisticsServiceCharges()) {
                BigDecimal percent = logisticsServiceCharge.getTaxPercent();
                VATAmount itemVATAmount = new VATAmount(
                    logisticsServiceCharge.getTotalAmount(),
                    logisticsServiceCharge.getTotalAmount()
                        .multiply(percent).divide(new BigDecimal(100)));
                VATAmount current = hm.get(percent);
                if (current == null) {
                    hm.put(percent, itemVATAmount);
                } else {
                    hm.put(percent, current.add(itemVATAmount));
                }
            }
        }

        if (trans.getZFCharges() != null) {
            for (IZUGFeRDAllowanceCharge charge : trans.getZFCharges()) {
                BigDecimal percent = charge.getTaxPercent();
                VATAmount itemVATAmount = new VATAmount(
                    charge.getTotalAmount(), charge.getTotalAmount()
                    .multiply(percent).divide(new BigDecimal(100)));
                VATAmount current = hm.get(percent);
                if (current == null) {
                    hm.put(percent, itemVATAmount);
                } else {
                    hm.put(percent, current.add(itemVATAmount));
                }
            }
        }

        return hm;
    }

    ZUGFeRDTransactionModelConverter withTest(boolean isTest) {
        this.isTest = isTest;
        return this;
    }

    private class LineCalc {

        private BigDecimal totalGross;
        private BigDecimal itemTotalNetAmount;
        private BigDecimal itemTotalVATAmount;
        private BigDecimal itemNetAmount;

        public LineCalc(IZUGFeRDExportableItem currentItem) {
            BigDecimal totalAllowance = BigDecimal.ZERO;
            BigDecimal totalCharge = BigDecimal.ZERO;

            if (currentItem.getItemAllowances() != null) {
                for (IZUGFeRDAllowanceCharge itemAllowance : currentItem
                    .getItemAllowances()) {
                    totalAllowance = itemAllowance.getTotalAmount().add(
                        totalAllowance);
                }
            }

            if (currentItem.getItemCharges() != null) {
                for (IZUGFeRDAllowanceCharge itemCharge : currentItem
                    .getItemCharges()) {
                    totalCharge = itemCharge.getTotalAmount().add(totalCharge);
                }
            }

            BigDecimal multiplicator = currentItem.getProduct().getVATPercent()
                .divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_UP)
                .add(BigDecimal.ONE);
            // priceGross=currentItem.getPrice().multiply(multiplicator);

            totalGross = currentItem.getPrice()
                .multiply(currentItem.getQuantity())
                .subtract(totalAllowance).add(totalCharge)
                .multiply(multiplicator);
            itemTotalNetAmount = currentItem.getPrice()
                .multiply(currentItem.getQuantity())
                .subtract(totalAllowance).add(totalCharge)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
            itemTotalVATAmount = totalGross.subtract(itemTotalNetAmount);
            itemNetAmount = currentItem
                .getPrice()
                .multiply(currentItem.getQuantity())
                .subtract(totalAllowance)
                .add(totalCharge)
                .divide(currentItem.getQuantity(), 4,
                    BigDecimal.ROUND_HALF_UP);
        }

        public BigDecimal getItemTotalNetAmount() {
            return itemTotalNetAmount;
        }

        public BigDecimal getItemTotalVATAmount() {
            return itemTotalVATAmount;
        }

        public BigDecimal getItemNetAmount() {
            return itemNetAmount;
        }

    }

    private class Totals {
        private BigDecimal totalNetAmount;
        private BigDecimal totalGrossAmount;
        private BigDecimal lineTotalAmount;
        private BigDecimal totalTaxAmount;

        public Totals() {
            BigDecimal res = BigDecimal.ZERO;
            for (IZUGFeRDExportableItem currentItem : trans.getZFItems()) {
                LineCalc lc = new LineCalc(currentItem);
                res = res.add(lc.getItemTotalNetAmount());
            }
            // Set line total
            this.lineTotalAmount = res;

            if (trans.getZFAllowances() != null) {
                for (IZUGFeRDAllowanceCharge headerAllowance : trans
                    .getZFAllowances()) {
                    res = res.subtract(headerAllowance.getTotalAmount());
                }
            }

            if (trans.getZFLogisticsServiceCharges() != null) {
                for (IZUGFeRDAllowanceCharge logisticsServiceCharge : trans
                    .getZFLogisticsServiceCharges()) {
                    res = res.add(logisticsServiceCharge.getTotalAmount());
                }
            }

            if (trans.getZFCharges() != null) {
                for (IZUGFeRDAllowanceCharge charge : trans.getZFCharges()) {
                    res = res.add(charge.getTotalAmount());
                }
            }

            // Set total net amount
            this.totalNetAmount = res;

            HashMap<BigDecimal, VATAmount> VATPercentAmountMap = getVATPercentAmountMap();
            for (BigDecimal currentTaxPercent : VATPercentAmountMap.keySet()) {
                VATAmount amount = VATPercentAmountMap.get(currentTaxPercent);
                res = res.add(amount.getCalculated());
            }

            // Set total gross amount
            this.totalGrossAmount = res;

            this.totalTaxAmount = this.totalGrossAmount
                .subtract(this.totalNetAmount);
        }

        public BigDecimal getTotalNet() {
            return totalNetAmount;
        }

        public BigDecimal getTotalGross() {
            return totalGrossAmount;
        }

        public BigDecimal getLineTotal() {
            return lineTotalAmount;
        }

        public BigDecimal getTaxTotal() {
            return totalTaxAmount;
        }

    }
}
