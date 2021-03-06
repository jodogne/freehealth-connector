/*
 *
 * Copyright (C) 2018 Taktik SA
 *
 * This file is part of FreeHealthConnector.
 *
 * FreeHealthConnector is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation.
 *
 * FreeHealthConnector is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with FreeHealthConnector.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.taktik.freehealth.middleware.service.impl

import be.fgov.ehealth.hubservices.core.v3.ConsentType
import be.fgov.ehealth.hubservices.core.v3.GetHCPartyConsentRequest
import be.fgov.ehealth.hubservices.core.v3.GetPatientConsentRequest
import be.fgov.ehealth.hubservices.core.v3.GetPatientRequest
import be.fgov.ehealth.hubservices.core.v3.GetTherapeuticLinkRequest
import be.fgov.ehealth.hubservices.core.v3.GetTransactionListRequest
import be.fgov.ehealth.hubservices.core.v3.GetTransactionRequest
import be.fgov.ehealth.hubservices.core.v3.GetTransactionSetRequest
import be.fgov.ehealth.hubservices.core.v3.HCPartyIdType
import be.fgov.ehealth.hubservices.core.v3.PatientIdType
import be.fgov.ehealth.hubservices.core.v3.PutPatientConsentRequest
import be.fgov.ehealth.hubservices.core.v3.PutPatientRequest
import be.fgov.ehealth.hubservices.core.v3.PutTherapeuticLinkRequest
import be.fgov.ehealth.hubservices.core.v3.PutTransactionRequest
import be.fgov.ehealth.hubservices.core.v3.PutTransactionResponse
import be.fgov.ehealth.hubservices.core.v3.PutTransactionSetRequest
import be.fgov.ehealth.hubservices.core.v3.PutTransactionSetResponse
import be.fgov.ehealth.hubservices.core.v3.RequestType
import be.fgov.ehealth.hubservices.core.v3.SelectGetHCPartyConsentType
import be.fgov.ehealth.hubservices.core.v3.SelectGetHCPartyPatientConsentType
import be.fgov.ehealth.hubservices.core.v3.SelectGetPatientConsentType
import be.fgov.ehealth.hubservices.core.v3.SelectGetPatientType
import be.fgov.ehealth.hubservices.core.v3.SelectGetTransactionListType
import be.fgov.ehealth.hubservices.core.v3.SelectGetTransactionType
import be.fgov.ehealth.hubservices.core.v3.TherapeuticLinkType
import be.fgov.ehealth.hubservices.core.v3.TransactionBaseType
import be.fgov.ehealth.hubservices.core.v3.TransactionWithPeriodType
import be.fgov.ehealth.standards.kmehr.cd.v1.CDADDRESS
import be.fgov.ehealth.standards.kmehr.cd.v1.CDADDRESSschemes
import be.fgov.ehealth.standards.kmehr.cd.v1.CDCONSENT
import be.fgov.ehealth.standards.kmehr.cd.v1.CDCONSENTschemes
import be.fgov.ehealth.standards.kmehr.cd.v1.CDCONSENTvalues
import be.fgov.ehealth.standards.kmehr.cd.v1.CDCOUNTRY
import be.fgov.ehealth.standards.kmehr.cd.v1.CDCOUNTRYschemes
import be.fgov.ehealth.standards.kmehr.cd.v1.CDHCPARTY
import be.fgov.ehealth.standards.kmehr.cd.v1.CDHCPARTYschemes
import be.fgov.ehealth.standards.kmehr.cd.v1.CDSEX
import be.fgov.ehealth.standards.kmehr.cd.v1.CDSEXvalues
import be.fgov.ehealth.standards.kmehr.cd.v1.CDTHERAPEUTICLINK
import be.fgov.ehealth.standards.kmehr.cd.v1.CDTHERAPEUTICLINKschemes
import be.fgov.ehealth.standards.kmehr.id.v1.IDHCPARTY
import be.fgov.ehealth.standards.kmehr.id.v1.IDHCPARTYschemes
import be.fgov.ehealth.standards.kmehr.id.v1.IDKMEHR
import be.fgov.ehealth.standards.kmehr.id.v1.IDKMEHRschemes
import be.fgov.ehealth.standards.kmehr.id.v1.IDPATIENT
import be.fgov.ehealth.standards.kmehr.id.v1.IDPATIENTschemes
import be.fgov.ehealth.standards.kmehr.schema.v1.AddressType
import be.fgov.ehealth.standards.kmehr.schema.v1.AuthorType
import be.fgov.ehealth.standards.kmehr.schema.v1.CountryType
import be.fgov.ehealth.standards.kmehr.schema.v1.DateType
import be.fgov.ehealth.standards.kmehr.schema.v1.HcpartyType
import be.fgov.ehealth.standards.kmehr.schema.v1.Kmehrmessage
import be.fgov.ehealth.standards.kmehr.schema.v1.PersonType
import be.fgov.ehealth.standards.kmehr.schema.v1.SexType
import com.google.gson.Gson
import ma.glasnost.orika.MapperFacade
import org.apache.commons.lang.StringUtils
import org.joda.time.DateTime
import org.springframework.stereotype.Service
import org.taktik.connector.business.therlink.domain.TherapeuticLink
import org.taktik.connector.technical.config.ConfigFactory
import org.taktik.connector.technical.utils.IdentifierType
import org.taktik.connector.technical.utils.MarshallerHelper
import org.taktik.freehealth.middleware.domain.Consent
import org.taktik.freehealth.middleware.domain.HcPartyConsent
import org.taktik.freehealth.middleware.domain.Patient
import org.taktik.freehealth.middleware.domain.TransactionSummary
import org.taktik.freehealth.middleware.dto.Address
import org.taktik.freehealth.middleware.dto.common.AuthorDto
import org.taktik.freehealth.middleware.dto.common.Gender
import org.taktik.freehealth.middleware.dto.common.KmehrCd
import org.taktik.freehealth.middleware.dto.common.KmehrId
import org.taktik.freehealth.middleware.service.HubService
import org.taktik.freehealth.middleware.service.STSService
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashSet

@Service
class HubServiceImpl(val stsService: STSService, val mapper: MapperFacade) : HubService {
    private val config = ConfigFactory.getConfigValidator(listOf())
    private val freehealthHubService: org.taktik.connector.business.hubv3.service.HubTokenService =
        org.taktik.connector.business.hubv3.service.impl.HubTokenServiceImpl()
    private val nisCodesPerZip =
        Gson().fromJson<Map<String, String>>(
            this.javaClass.getResourceAsStream("/NisCodes.json").bufferedReader(Charsets.UTF_8),
            HashMap<String, String>().javaClass
        )

    override fun getHcPartyConsent(
        endpoint: String,
        keystoreId: UUID,
        tokenId: UUID,
        passPhrase: String,
        hcpNihii: String,
        hcpSsin: String,
        hcpZip: String
    ): HcPartyConsent? {
        val samlToken =
            stsService.getSAMLToken(tokenId, keystoreId, passPhrase)
                ?: throw IllegalArgumentException("Cannot obtain token for Hub operations")
        val hcPartyConsent =
            freehealthHubService.getHCPartyConsent(
                endpoint,
                samlToken,
                stsService.getKeyStore(keystoreId, passPhrase)!!,
                passPhrase,
                GetHCPartyConsentRequest().apply {
                    request = createRequestType(hcpNihii, hcpSsin, hcpZip, false)
                    select = SelectGetHCPartyConsentType().apply {
                        hcparty = HCPartyIdType().apply {
                            ids.add(IDHCPARTY().apply { s = IDHCPARTYschemes.ID_HCPARTY; sv = "1.0"; value = hcpNihii })
                            ids.add(IDHCPARTY().apply { s = IDHCPARTYschemes.INSS; sv = "1.0"; value = hcpSsin })
                        }
                    }
                })
        return hcPartyConsent.consent?.let {
            mapper.map(it, HcPartyConsent::class.java)
        }
    }

    override fun getPatientConsent(
        endpoint: String,
        keystoreId: UUID,
        tokenId: UUID,
        passPhrase: String,
        hcpNihii: String,
        hcpSsin: String,
        hcpZip: String,
        patientSsin: String
    ): Consent? {
        val samlToken =
            stsService.getSAMLToken(tokenId, keystoreId, passPhrase)
                ?: throw IllegalArgumentException("Cannot obtain token for Hub operations")
        val patientConsent =
            freehealthHubService.getPatientConsent(
                endpoint,
                samlToken,
                stsService.getKeyStore(keystoreId, passPhrase)!!,
                passPhrase,
                GetPatientConsentRequest().apply {
                    request = createRequestType(hcpNihii, hcpSsin, hcpZip, false)
                    select = SelectGetPatientConsentType().apply {
                        patient =
                            PatientIdType().apply {
                                ids.add(IDPATIENT().apply {
                                    this.s =
                                        IDPATIENTschemes.INSS; this.sv = "1.0"; this.value = patientSsin
                                })
                            }
                        consent =
                            be.fgov.ehealth.hubservices.core.v3.Consent()
                                .apply {
                                    cds.add(CDCONSENT().apply {
                                        s = CDCONSENTschemes.CD_CONSENTTYPE; sv =
                                        "1.0"; value = CDCONSENTvalues.RETROSPECTIVE
                                    })
                                }
                    }
                })
        return patientConsent.consent?.let {
            mapper.map(it, Consent::class.java)
        }
    }

    override fun registerPatientConsent(
        endpoint: String,
        keystoreId: UUID,
        tokenId: UUID,
        passPhrase: String,
        hcpNihii: String,
        hcpSsin: String,
        hcpZip: String,
        patientSsin: String,
        patientEidCardNumber: String?
    ) {
        val samlToken =
            stsService.getSAMLToken(tokenId, keystoreId, passPhrase)
                ?: throw IllegalArgumentException("Cannot obtain token for Hub operations")
        val patientConsent =
            freehealthHubService.putPatientConsent(
                endpoint,
                samlToken,
                stsService.getKeyStore(keystoreId, passPhrase)!!,
                passPhrase,
                PutPatientConsentRequest().apply {
                    request = createRequestType(hcpNihii, hcpSsin, hcpZip, false)
                    consent = ConsentType().apply {
                        patient = PatientIdType().apply {
                            ids.add(IDPATIENT().apply {
                                this.s = IDPATIENTschemes.INSS; this.sv = "1.0"; this.value =
                                patientSsin
                            })
                            patientEidCardNumber?.let {
                                ids.add(IDPATIENT().apply {
                                    this.s =
                                        IDPATIENTschemes.EID_CARDNO; this.sv = "1.0"; this.value = patientEidCardNumber
                                })
                            }
                        }
                        cds.add(CDCONSENT().apply {
                            s = CDCONSENTschemes.CD_CONSENTTYPE; sv = "1.0"; value =
                            CDCONSENTvalues.RETROSPECTIVE
                        })
                        author = AuthorType().apply { hcparties.add(request.author.hcparties.first()) }
                    }
                })
    }

    override fun registerTherapeuticLink(
        endpoint: String,
        keystoreId: UUID,
        tokenId: UUID,
        passPhrase: String,
        hcpNihii: String,
        hcpSsin: String,
        hcpZip: String,
        patientSsin: String,
        patientEidCardNumber: String?
    ) {
        val samlToken =
            stsService.getSAMLToken(tokenId, keystoreId, passPhrase)
                ?: throw IllegalArgumentException("Cannot obtain token for Hub operations")
        val link =
            freehealthHubService.putTherapeuticLink(
                endpoint,
                samlToken,
                stsService.getKeyStore(keystoreId, passPhrase)!!,
                passPhrase,
                PutTherapeuticLinkRequest().apply {
                    request = createRequestType(hcpNihii, hcpSsin, hcpZip, false)
                    therapeuticlink = TherapeuticLinkType().apply {
                        patient = PatientIdType().apply {
                            ids.add(IDPATIENT().apply {
                                this.s = IDPATIENTschemes.INSS; this.sv = "1.0"; this.value =
                                patientSsin
                            })
                            patientEidCardNumber?.let {
                                ids.add(IDPATIENT().apply {
                                    this.s =
                                        IDPATIENTschemes.EID_CARDNO; this.sv = "1.0"; this.value = patientEidCardNumber
                                })
                            }
                        }
                    }
                })
    }

    override fun getTherapeuticLinks(
        endpoint: String,
        keystoreId: UUID,
        tokenId: UUID,
        passPhrase: String,
        hcpNihii: String,
        hcpSsin: String,
        hcpZip: String,
        patientSsin: String,
        therLinkType: String?,
        from: Instant?,
        to: Instant?
    ): List<TherapeuticLink> {
        val samlToken =
            stsService.getSAMLToken(tokenId, keystoreId, passPhrase)
                ?: throw IllegalArgumentException("Cannot obtain token for Hub operations")
        val therapeuticLinkResponse =
            freehealthHubService.getTherapeuticLink(
                endpoint,
                samlToken,
                stsService.getKeyStore(keystoreId, passPhrase)!!,
                passPhrase,
                GetTherapeuticLinkRequest().apply {
                    request = createRequestType(hcpNihii, hcpSsin, hcpZip, false)
                    select = SelectGetHCPartyPatientConsentType().apply {
                        therLinkType?.let {
                            cds.add(CDTHERAPEUTICLINK().apply {
                                s =
                                    CDTHERAPEUTICLINKschemes.CD_THERAPEUTICLINKTYPE; sv = "1.0"; value = it
                            })
                        }
                        patientsAndHcparties.add(HCPartyIdType().apply {
                            ids.add(IDHCPARTY().apply { s = IDHCPARTYschemes.ID_HCPARTY; sv = "1.0"; value = hcpNihii })
                            ids.add(IDHCPARTY().apply { s = IDHCPARTYschemes.INSS; sv = "1.0"; value = hcpSsin })
                        })
                        patientsAndHcparties.add(PatientIdType().apply {
                            ids.add(IDPATIENT().apply {
                                this.s = IDPATIENTschemes.INSS; this.sv = "1.0"; this.value =
                                patientSsin
                            })
                        })
                        begindate = from?.let { DateTime(it.toEpochMilli()) } ?: DateTime.now()
                        enddate = from?.let { DateTime(it.toEpochMilli()) } ?: DateTime.now()
                    }
                })
        return therapeuticLinkResponse.therapeuticlinklist?.let {
            it.therapeuticlinks.map { mapper.map(it, TherapeuticLink::class.java) }
        } ?: listOf()
    }

    override fun putPatient(
        endpoint: String,
        keystoreId: UUID,
        tokenId: UUID,
        passPhrase: String,
        hcpNihii: String,
        hcpSsin: String,
        hcpZip: String,
        patientSsin: String,
        firstName: String,
        lastName: String,
        gender: Gender,
        dateOfBirth: LocalDateTime
    ): Patient? {
        val samlToken =
            stsService.getSAMLToken(tokenId, keystoreId, passPhrase)
                ?: throw IllegalArgumentException("Cannot obtain token for Hub operations")
        val transaction =
            freehealthHubService.putPatient(
                endpoint,
                samlToken,
                stsService.getKeyStore(keystoreId, passPhrase)!!,
                passPhrase,
                PutPatientRequest().apply {
                    request = createRequestType(hcpNihii, hcpSsin, hcpZip, true)
                    patient = PersonType().apply {
                        firstnames.add(firstName)
                        familyname = lastName
                        sex =
                            SexType().apply {
                                cd =
                                    CDSEX().apply { sv = "1.0"; value = CDSEXvalues.fromValue(gender.name) }
                            }
                        ids.add(IDPATIENT().apply {
                            this.s = IDPATIENTschemes.INSS; this.sv = "1.0"; this.value =
                            patientSsin
                        })
                        birthdate =
                            DateType().apply {
                                date =
                                    org.joda.time.DateTime(
                                        dateOfBirth.year,
                                        dateOfBirth.monthValue,
                                        dateOfBirth.dayOfMonth,
                                        0,
                                        0
                                    )
                            }
                    }
                })
        return transaction.patient?.let {
            Patient().apply {
                this.firstName = it.firstnames.firstOrNull()
                this.lastName = it.familyname
                this.gender = it.sex?.cd?.value?.value()?.let { Gender.valueOf(it) } ?: Gender.unknown
                ssin = it.ids.find { it.s == IDPATIENTschemes.ID_PATIENT }?.value
                addresses = it.addresses?.map { mapper.map(it, Address::class.java) }?.toMutableSet() ?: HashSet()
            }
        }
    }

    override fun getPatient(
        endpoint: String,
        keystoreId: UUID,
        tokenId: UUID,
        passPhrase: String,
        hcpNihii: String,
        hcpSsin: String,
        hcpZip: String,
        patientSsin: String
    ): Patient? {
        val samlToken =
            stsService.getSAMLToken(tokenId, keystoreId, passPhrase)
                ?: throw IllegalArgumentException("Cannot obtain token for Hub operations")
        val patient =
            freehealthHubService.getPatient(
                endpoint,
                samlToken,
                stsService.getKeyStore(keystoreId, passPhrase)!!,
                passPhrase,
                GetPatientRequest().apply {
                    request = createRequestType(hcpNihii, hcpSsin, hcpZip, true)
                    select = SelectGetPatientType().apply {
                        patient =
                            PatientIdType().apply {
                                ids.add(IDPATIENT().apply {
                                    this.s =
                                        IDPATIENTschemes.INSS; this.sv = "1.0"; this.value = patientSsin
                                })
                            }
                    }
                })
        return patient.patient?.let {
            Patient().apply {
                firstName = it.firstnames.firstOrNull()
                lastName = it.familyname
                gender = it.sex?.cd?.value?.value()?.let { Gender.valueOf(it) } ?: Gender.unknown
                ssin = it.ids.find { it.s == IDPATIENTschemes.ID_PATIENT }?.value
                addresses = it.addresses?.map { mapper.map(it, Address::class.java) }?.toMutableSet() ?: HashSet()
            }
        }
    }

    override fun getTransaction(
        endpoint: String,
        keystoreId: UUID,
        tokenId: UUID,
        passPhrase: String,
        hcpNihii: String,
        hcpSsin: String,
        hcpZip: String,
        ssin: String,
        sv: String,
        sl: String,
        value: String
    ): String {
        val samlToken =
            stsService.getSAMLToken(tokenId, keystoreId, passPhrase)
                ?: throw IllegalArgumentException("Cannot obtain token for Hub operations")
        val transaction =
            freehealthHubService.getTransaction(
                endpoint,
                samlToken,
                stsService.getKeyStore(keystoreId, passPhrase)!!,
                passPhrase,
                GetTransactionRequest().apply {
                    request = createRequestType(hcpNihii, hcpSsin, hcpZip, true)
                    select = SelectGetTransactionType().apply {
                        patient =
                            PatientIdType().apply {
                                ids.add(IDPATIENT().apply {
                                    this.s =
                                        IDPATIENTschemes.INSS; this.sv = "1.0"; this.value = ssin
                                })
                            }
                        transaction = TransactionBaseType().apply {
                            id =
                                IDKMEHR().apply {
                                    this.s = IDKMEHRschemes.LOCAL; this.sv = sv; this.sl =
                                    sl; this.value = value
                                }
                        }
                    }
                })

        return MarshallerHelper(
            Kmehrmessage::class.java,
            Kmehrmessage::class.java
        ).toXMLByteArray(transaction.kmehrmessage).toString(Charsets.UTF_8)
    }

    override fun putTransaction(
        endpoint: String,
        hubId: Long,
        hubApplication: String,
        keystoreId: UUID,
        tokenId: UUID,
        passPhrase: String,
        hcpNihii: String,
        hcpSsin: String,
        hcpZip: String,
        ssin: String,
        transaction: String
    ): PutTransactionResponse {
        val samlToken =
            stsService.getSAMLToken(tokenId, keystoreId, passPhrase)
                ?: throw IllegalArgumentException("Cannot obtain token for Hub operations")
        val marshallerHelper = MarshallerHelper(Kmehrmessage::class.java, Kmehrmessage::class.java)
        return freehealthHubService.putTransaction(
            endpoint,
            hubId,
            hubApplication,
            samlToken,
            stsService.getKeyStore(keystoreId, passPhrase)!!,
            passPhrase,
            PutTransactionRequest().apply {
                request = createRequestType(hcpNihii, hcpSsin, hcpZip, true)
                kmehrmessage =
                    marshallerHelper.toObject(transaction.toByteArray(Charsets.UTF_8))
            })
    }

    override fun getTransactionsList(
        endpoint: String,
        keystoreId: UUID,
        tokenId: UUID,
        passPhrase: String,
        hcpNihii: String,
        hcpSsin: String,
        hcpZip: String,
        ssin: String,
        from: Long?,
        to: Long?,
        authorNihii: String?,
        authorSsin: String?,
        isGlobal: Boolean
    ): List<TransactionSummary> {
        val samlToken =
            stsService.getSAMLToken(tokenId, keystoreId, passPhrase)
                ?: throw IllegalArgumentException("Cannot obtain token for Hub operations")
        val transactionList =
            freehealthHubService.getTransactionList(
                endpoint,
                samlToken,
                stsService.getKeyStore(keystoreId, passPhrase)!!,
                passPhrase,
                GetTransactionListRequest().apply {
                    request = createRequestType(hcpNihii, hcpSsin, hcpZip, false)
                    select = SelectGetTransactionListType().apply {
                        patient =
                            PatientIdType().apply {
                                ids.add(IDPATIENT().apply {
                                    s = IDPATIENTschemes.INSS; sv =
                                    "1.0"; value = ssin
                                })
                            }
                        transaction = TransactionWithPeriodType().apply {
                            from?.let { begindate = DateTime(from) }
                            to?.let { enddate = DateTime(to) }
                            if (!StringUtils.isEmpty(authorNihii) || !StringUtils.isEmpty(authorSsin)) {
                                author = AuthorType().apply {
                                    hcparties.add(HcpartyType().apply {
                                        cds.add(CDHCPARTY().apply {
                                            s = CDHCPARTYschemes.CD_HCPARTY; sv = "1.1"; value =
                                            "*"
                                        })
                                        ids.add(IDHCPARTY().apply {
                                            if (StringUtils.isEmpty(authorSsin)) {
                                                s = IDHCPARTYschemes.INSS; sv = "1.0"; value = authorSsin
                                            } else {
                                                s = IDHCPARTYschemes.ID_HCPARTY; sv = "1.0"; value = authorNihii
                                            }
                                        })
                                    })
                                }
                            }
                        }
                    }
                })

        return transactionList.kmehrheader?.folder?.transactions?.map {
            TransactionSummary().apply {
                author = mapper.map(it.author, AuthorDto::class.java)
                ids = it.ids.map { KmehrId().apply { s = it?.s?.value(); sv = it?.sv; sl = it?.sl; value = it?.value } }
                cds = it.cds.map { KmehrCd().apply { s = it?.s?.value(); sv = it?.sv; sl = it?.sl; value = it?.value } }
                date = it.date.millis
                time = it.time.millis
                recorddatetime = it.recorddatetime.millis
                iscomplete = it.isIscomplete
                isvalidated = it.isIsvalidated
            }
        } ?: listOf()
    }

    override fun getTransactionSet(
        endpoint: String,
        keystoreId: UUID,
        tokenId: UUID,
        passPhrase: String,
        hcpNihii: String,
        hcpSsin: String,
        hcpZip: String,
        ssin: String,
        sv: String,
        sl: String,
        value: String
    ): String {
        val samlToken =
            stsService.getSAMLToken(tokenId, keystoreId, passPhrase)
                ?: throw IllegalArgumentException("Cannot obtain token for Hub operations")
        val transaction =
            freehealthHubService.getTransactionSet(
                endpoint,
                samlToken,
                stsService.getKeyStore(keystoreId, passPhrase)!!,
                passPhrase,
                GetTransactionSetRequest().apply {
                    request = createRequestType(hcpNihii, hcpSsin, hcpZip, true)
                    select = SelectGetTransactionType().apply {
                        patient =
                            PatientIdType().apply {
                                ids.add(IDPATIENT().apply {
                                    this.s =
                                        IDPATIENTschemes.INSS; this.sv = "1.0"; this.value = ssin
                                })
                            }
                        transaction = TransactionBaseType().apply {
                            id =
                                IDKMEHR().apply {
                                    this.s = IDKMEHRschemes.LOCAL; this.sv = sv; this.sl =
                                    sl; this.value = value
                                }
                        }
                    }
                })

        return MarshallerHelper(
            Kmehrmessage::class.java,
            Kmehrmessage::class.java
        ).toXMLByteArray(transaction.kmehrmessage).toString(Charsets.UTF_8)
    }

    override fun putTransactionSet(
        endpoint: String,
        hubId: Long,
        hubApplication: String,
        keystoreId: UUID,
        tokenId: UUID,
        passPhrase: String,
        hcpNihii: String,
        hcpSsin: String,
        hcpZip: String,
        ssin: String,
        transaction: String
    ): PutTransactionSetResponse {
        val samlToken =
            stsService.getSAMLToken(tokenId, keystoreId, passPhrase)
                ?: throw IllegalArgumentException("Cannot obtain token for Hub operations")
        val marshallerHelper = MarshallerHelper(Kmehrmessage::class.java, Kmehrmessage::class.java)
        return freehealthHubService.putTransactionSet(
            endpoint,
            hubId,
            hubApplication,
            samlToken,
            stsService.getKeyStore(keystoreId, passPhrase)!!,
            passPhrase,
            PutTransactionSetRequest().apply {
                request = createRequestType(hcpNihii, hcpSsin, hcpZip, true)
                kmehrmessage =
                    marshallerHelper.toObject(transaction.toByteArray(Charsets.UTF_8))
            })
    }

    private fun createRequestType(
        hcpNihii: String,
        hcpSsin: String,
        hcpZip: String,
        encrypted: Boolean = false
    ): RequestType {
        return RequestType().apply {
            date = DateTime.now()
            time = DateTime.now()
            id =
                IDKMEHR().apply {
                    s = IDKMEHRschemes.ID_KMEHR; sv = "1.0"; value =
                    "$hcpNihii.${Instant.now().toEpochMilli()}"
                }
            author = AuthorType().apply {
                hcparties.add(HcpartyType().apply {
                    ids.add(IDHCPARTY().apply { s = IDHCPARTYschemes.ID_HCPARTY; sv = "1.0"; value = hcpNihii })
                    ids.add(IDHCPARTY().apply { s = IDHCPARTYschemes.INSS; sv = "1.0"; value = hcpSsin })
                    cds.add(CDHCPARTY().apply { s = CDHCPARTYschemes.CD_HCPARTY; sv = "1.1"; value = "persphysician" })

                    if (encrypted) {
                        ids.add(IDHCPARTY().apply {
                            s = IDHCPARTYschemes.ID_ENCRYPTION_ACTOR; sv = "1.0"; value =
                            hcpNihii.substring(0, 8)
                        })
                        cds.add(CDHCPARTY().apply {
                            s = CDHCPARTYschemes.CD_ENCRYPTION_ACTOR; sv = "1.1"; value =
                            IdentifierType.NIHII.getType(48)
                        })
                    }
                    firstname = "Antoine"
                    familyname = "Baudoux"
                    addresses.add(AddressType().apply {
                        cds.add(CDADDRESS().apply { s = CDADDRESSschemes.CD_ADDRESS; sv = "1.1"; value = "work" })
                        country =
                            CountryType().apply {
                                cd =
                                    CDCOUNTRY().apply { s = CDCOUNTRYschemes.CD_FED_COUNTRY; sv = "1.2"; value = "be" }
                            }
                        zip = hcpZip
                        nis = nisCodesPerZip[hcpZip]
                    })
                })
                hcparties.add(HcpartyType().apply {
                    ids.add(IDHCPARTY().apply {
                        s = IDHCPARTYschemes.LOCAL; sl = "endusersoftwareinfo"; sv =
                        "1.0"; value = config.getProperty("package.name") ?: "iCure"
                    })
                    cds.add(CDHCPARTY().apply { s = CDHCPARTYschemes.CD_HCPARTY; sv = "1.1"; value = "application" })
                })
            }
        }
    }
}
