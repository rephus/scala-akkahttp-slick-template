package todo.view

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.{Locale, UUID}

import spray.json.{DefaultJsonProtocol, DeserializationException, JsNumber, JsString, JsValue, JsonFormat}
import todo.model.Task
import akka.http.javadsl.unmarshalling.StringUnmarshaller
import akka.http.javadsl.marshalling.Marshaller._
import TaskJsonProtocol.UUIDFormat

object TaskJsonProtocol extends DefaultJsonProtocol {


  implicit object UUIDFormat extends JsonFormat[UUID] {
    def write(uuid: UUID) = JsString(uuid.toString)
    def read(value: JsValue) = {
      value match {
        case JsString(uuid) => UUID.fromString(uuid)
        case _              => throw new DeserializationException("Expected hexadecimal UUID string")
      }
    }
  }
  implicit object timestampFormat extends JsonFormat[Timestamp] {
    val format = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    def write(ts: Timestamp) = JsString(new SimpleDateFormat(format).format(ts))
    def read(value: JsValue) = {
      value match {
        case JsString(datetime) => new Timestamp(new SimpleDateFormat(format).parse(datetime).getTime())
        case _              => throw new DeserializationException("Expected hexadecimal UUID string")
      }
    }
  }


  implicit val taskFormat = jsonFormat6(Task)

}

/*
import java.sql.Date
import todo.model.{Account, Fx, Payment}
import spray.json._

object PaymentJsonProtocol extends DefaultJsonProtocol {

  implicit object DateFormat extends JsonFormat[Date] {
    def write(obj: Date) = JsString(obj.toString)
    def read(json: JsValue) = json match {
      case JsString(time) => Date.valueOf(time)
      case _ => throw new DeserializationException("Date expected")
    }
  }

  // This is another quick alternative to create automnatic one-liner json formats for all the classes
  // however, it doesn't match our schema, as fields are returned on CamelCase (instead of snake_case json standard)
  // and doesn't contain foreign keys on payments

  //implicit val paymentFormat = jsonFormat21(Payment)

  implicit object AccountFormat extends RootJsonFormat[Account] {
    def write(account: Account) = JsObject(
      "id" -> JsString(account.id.get),
      "account_name" -> JsString(account.accountName),
      "account_number" -> JsNumber(account.accountNumber),
      "number_code" -> JsString(account.numberCode),
      "account_type" -> JsNumber(account.accountType),
      "address" -> JsString(account.address),
      "bank_id" -> JsNumber(account.bankId),
      "bank_id_code" -> JsString(account.bankIdCode),
      "name" -> JsString(account.name)
    )
    def read(value: JsValue) =  value.asJsObject.getFields(
      "id", "account_name", "account_number",  "number_code", "account_type", "address",
        "bank_id", "bank_id_code", "name") match {
      case Seq(JsString(id), JsString(accountName), JsNumber(accountNumber),  JsString(numberCode),
      JsNumber(accountType), JsString(address), JsNumber(bankId),  JsString(bankIdCode), JsString(name)
      ) =>
        Account(
          id = Some(id), accountName = accountName, accountNumber = accountNumber, numberCode = numberCode,
          accountType = accountType.toInt, address = address, bankId = bankId.toInt, bankIdCode = bankIdCode, name = name
        )

      case _ => throw new DeserializationException("Account expected")
    }
  }


  implicit object FxFormat extends RootJsonFormat[Fx] {
    def write(fx: Fx) = JsObject(
      "contract_reference" -> JsString(fx.contractReference),
      "exchange_rate" -> JsNumber(fx.exchangeRate),
      "original_currency" -> JsString(fx.originalCurrency),
      "original_amount" -> JsNumber(fx.originalAmount)
    )
    def read(value: JsValue) = {
      value.asJsObject.getFields("contract_reference", "exchange_rate", "original_currency",  "original_amount") match {
        case Seq(JsString(contractReference), JsNumber(exchangeRate), JsString(originalCurrency),  JsNumber(originalAmount)) =>
          Fx(contractReference = contractReference,
            exchangeRate = exchangeRate.toFloat,
            originalCurrency = originalCurrency,
            originalAmount = originalAmount.toFloat
          )

        case _ => throw new DeserializationException("Fx expected")
      }
    }
  }

  def senderCharges(senderCharges: String): JsValue = {

    //Sender charges format in DB
    //eg: "123.55 USD;56.55 GBP;35.66 EUR"
    val charges = senderCharges.split(";").map(_.split(" "))
    val chargeJson = charges.map(charge => JsObject(
      "amount" -> JsNumber(charge.head),
      "currency" -> JsString(charge.last)
    )).toVector
     JsArray(chargeJson)
  }
  def parseSenderCharges(senderCharges: JsValue): String = {
    case class Money(amount: Float, currency: String)
    implicit val moneyFormat = jsonFormat2(Money)

    val money = senderCharges.convertTo[Seq[Money]]
    money.map(m => s"${m.amount} ${m.currency}").mkString(";")
  }
  implicit object PaymentFormat extends RootJsonFormat[Payment] {

    def write(payment: Payment) = {

      JsObject(
        "id" -> JsString(payment.id.getOrElse("")),
        "amount" -> JsNumber(payment.amount),
        "currency" -> JsString(payment.currency),
        "bearer_code" -> JsString(payment.bearerCode),
        "sender_charges" -> senderCharges(payment.senderCharges),

        "receiver_charges_amount" -> JsNumber(payment.receiverChargesAmount),
        "receiver_charges_currency" -> JsString(payment.receiverChargesCurrency),
        "end_to_end_reference" -> JsString(payment.endToEndReference),
        "numeric_reference" -> JsNumber(payment.numericReference),

        "payment_id" -> JsNumber(payment.paymentId),
        "payment_purpose" -> JsString(payment.paymentPurpose),
        "payment_scheme" -> JsString(payment.paymentScheme),
        "payment_type" -> JsString(payment.paymentType),
        "processing_date" -> JsString(payment.processingDate.toString),

        "reference" -> JsString(payment.reference),
        "scheme_payment_subtype" -> JsString(payment.schemePaymentSubtype),
        "scheme_payment_type" -> JsString(payment.schemePaymentType),

        "fx" -> payment.fx.toJson,

        // Show foreign key ids
        "beneficiary_id" -> JsString(payment.beneficiaryId),
        "sponsor_id" -> JsString(payment.sponsorId),
        "debtor_id" -> JsString(payment.debtorId),

        // Full foreign objects
        "beneficiary" -> payment.beneficiary.map(_.toJson).getOrElse(JsNull),
        "debtor" -> payment.debtor.map(_.toJson).getOrElse(JsNull),
        "sponsor" -> payment.sponsor.map(_.toJson).getOrElse(JsNull)

      )
    }
    def read(value: JsValue) = {
      value.asJsObject.getFields("id", "amount", "currency", "bearer_code", "sender_charges",
        "receiver_charges_amount", "receiver_charges_currency", "end_to_end_reference", "numeric_reference",
        "payment_id", "payment_purpose", "payment_scheme", "payment_type", "processing_date",
        "reference", "scheme_payment_subtype", "scheme_payment_type",
        "fx",
        //references
        "beneficiary_id", "sponsor_id", "debtor_id") match {
        case Seq(
        JsString(id),
        JsNumber(amount),
        JsString(currency),
        JsString(bearerCode),
        senderCharges,

        JsNumber(receiverChargesAmount),
        JsString(receiverChargesCurrency),
        JsString(endToEndReference),
        JsNumber(numericReference),

        JsNumber(paymentId),
        JsString(paymentPurpose),
        JsString(paymentScheme),
        JsString(paymentType),
        JsString(processingDate),

        JsString(reference),
        JsString(schemePaymentSubtype),
        JsString(schemePaymentType),
        fx,

        JsString(beneficiaryId),
        JsString(sponsorId),
        JsString(debtorId)
        ) => Payment(id=Some(id), amount= amount.toFloat, currency= currency, bearerCode= bearerCode,
          senderCharges= parseSenderCharges(senderCharges),
          receiverChargesAmount= receiverChargesAmount.toFloat, receiverChargesCurrency= receiverChargesCurrency,
          endToEndReference= endToEndReference, numericReference= numericReference.toInt,
          paymentId= paymentId, paymentPurpose= paymentPurpose, paymentScheme= paymentScheme, paymentType= paymentType,
          processingDate= Date.valueOf(processingDate),
          reference= reference, schemePaymentSubtype= schemePaymentSubtype, schemePaymentType= schemePaymentType,
          //fx
          fx = FxFormat.read(fx),
          //references
          beneficiaryId= beneficiaryId, sponsorId= sponsorId, debtorId=debtorId)

        case _ => throw new DeserializationException("Payment expected")
      }
    }

  }

}
*/