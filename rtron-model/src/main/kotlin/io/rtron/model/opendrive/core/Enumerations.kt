/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rtron.model.opendrive.core

enum class EDataQualityRawDataPostProcessing { RAW, CLEANED, PROCESSED, FUSED }

enum class EDataQualityRawDataSource { SENSOR, CADASTER, CUSTOM }

enum class EUnitDistance { METER, KILOMETER, FEET, MILE }

enum class EUnitSpeed { METER_PER_SECOND, MILES_PER_HOUR, KILOMETER_PER_HOUR }

enum class EUnitMass { KILOGRAM, TON }

enum class EUnitSlope { PERCENT }

enum class EUnit {
    METER,
    KILOMETER,
    FEET,
    MILE, // EUnitDistance
    METER_PER_SECOND,
    MILES_PER_HOUR,
    KILOMETER_PER_HOUR, // EUnitSpeed
    KILOGRAM,
    TON, // EUnitMass
    PERCENT, // EUnit
}

// see: https://github.com/lukes/ISO-3166-Countries-with-Regional-Codes/blob/master/all/all.csv
enum class ECountryCode {
    AF, // Afghanistan
    AX, // Åland Islands
    AL, // Albania
    DZ, // Algeria
    AS, // American Samoa
    AD, // Andorra
    AO, // Angola
    AI, // Anguilla
    AQ, // Antarctica
    AG, // Antigua and Barbuda
    AR, // Argentina
    AM, // Armenia
    AW, // Aruba
    AU, // Australia
    AT, // Austria
    AZ, // Azerbaijan
    BS, // Bahamas
    BH, // Bahrain
    BD, // Bangladesh
    BB, // Barbados
    BY, // Belarus
    BE, // Belgium
    BZ, // Belize
    BJ, // Benin
    BM, // Bermuda
    BT, // Bhutan
    BO, // Bolivia (Plurinational State of)
    BQ, // Bonaire, Sint Eustatius and Saba
    BA, // Bosnia and Herzegovina
    BW, // Botswana
    BV, // Bouvet Island
    BR, // Brazil
    IO, // British Indian Ocean Territory
    BN, // Brunei Darussalam
    BG, // Bulgaria
    BF, // Burkina Faso
    BI, // Burundi
    CV, // Cabo Verde
    KH, // Cambodia
    CM, // Cameroon
    CA, // Canada
    KY, // Cayman Islands
    CF, // Central African Republic
    TD, // Chad
    CL, // Chile
    CN, // China
    CX, // Christmas Island
    CC, // Cocos (Keeling) Islands
    CO, // Colombia
    KM, // Comoros
    CG, // Congo
    CD, // Congo, Democratic Republic of the
    CK, // Cook Islands
    CR, // Costa Rica
    CI, // Côte d'Ivoire
    HR, // Croatia
    CU, // Cuba
    CW, // Curaçao
    CY, // Cyprus
    CZ, // Czechia
    DK, // Denmark
    DJ, // Djibouti
    DM, // Dominica
    DO, // Dominican Republic
    EC, // Ecuador
    EG, // Egypt
    SV, // El Salvador
    GQ, // Equatorial Guinea
    ER, // Eritrea
    EE, // Estonia
    SZ, // Eswatini
    ET, // Ethiopia
    FK, // Falkland Islands (Malvinas)
    FO, // Faroe Islands
    FJ, // Fiji
    FI, // Finland
    FR, // France
    GF, // French Guiana
    PF, // French Polynesia
    TF, // French Southern Territories
    GA, // Gabon
    GM, // Gambia
    GE, // Georgia
    DE, // Germany
    GH, // Ghana
    GI, // Gibraltar
    GR, // Greece
    GL, // Greenland
    GD, // Grenada
    GP, // Guadeloupe
    GU, // Guam
    GT, // Guatemala
    GG, // Guernsey
    GN, // Guinea
    GW, // Guinea-Bissau
    GY, // Guyana
    HT, // Haiti
    HM, // Heard Island and McDonald Islands
    VA, // Holy See
    HN, // Honduras
    HK, // Hong Kong
    HU, // Hungary
    IS, // Iceland
    IN, // India
    ID, // Indonesia
    IR, // Iran (Islamic Republic of)
    IQ, // Iraq
    IE, // Ireland
    IM, // Isle of Man
    IL, // Israel
    IT, // Italy
    JM, // Jamaica
    JP, // Japan
    JE, // Jersey
    JO, // Jordan
    KZ, // Kazakhstan
    KE, // Kenya
    KI, // Kiribati
    KP, // Korea (Democratic People's Republic of)
    KR, // Korea, Republic of
    KW, // Kuwait
    KG, // Kyrgyzstan
    LA, // Lao People's Democratic Republic
    LV, // Latvia
    LB, // Lebanon
    LS, // Lesotho
    LR, // Liberia
    LY, // Libya
    LI, // Liechtenstein
    LT, // Lithuania
    LU, // Luxembourg
    MO, // Macao
    MG, // Madagascar
    MW, // Malawi
    MY, // Malaysia
    MV, // Maldives
    ML, // Mali
    MT, // Malta
    MH, // Marshall Islands
    MQ, // Martinique
    MR, // Mauritania
    MU, // Mauritius
    YT, // Mayotte
    MX, // Mexico
    FM, // Micronesia (Federated States of)
    MD, // Moldova, Republic of
    MC, // Monaco
    MN, // Mongolia
    ME, // Montenegro
    MS, // Montserrat
    MA, // Morocco
    MZ, // Mozambique
    MM, // Myanmar
    NA, // Namibia
    NR, // Nauru
    NP, // Nepal
    NL, // Netherlands
    NC, // New Caledonia
    NZ, // New Zealand
    NI, // Nicaragua
    NE, // Niger
    NG, // Nigeria
    NU, // Niue
    NF, // Norfolk Island
    MK, // North Macedonia
    MP, // Northern Mariana Islands
    NO, // Norway
    OM, // Oman
    PK, // Pakistan
    PW, // Palau
    PS, // Palestine, State of
    PA, // Panama
    PG, // Papua New Guinea
    PY, // Paraguay
    PE, // Peru
    PH, // Philippines
    PN, // Pitcairn
    PL, // Poland
    PT, // Portugal
    PR, // Puerto Rico
    QA, // Qatar
    RE, // Réunion
    RO, // Romania
    RU, // Russian Federation
    RW, // Rwanda
    BL, // Saint Barthélemy
    SH, // Saint Helena, Ascension and Tristan da Cunha
    KN, // Saint Kitts and Nevis
    LC, // Saint Lucia
    MF, // Saint Martin (French part)
    PM, // Saint Pierre and Miquelon
    VC, // Saint Vincent and the Grenadines
    WS, // Samoa
    SM, // San Marino
    ST, // Sao Tome and Principe
    SA, // Saudi Arabia
    SN, // Senegal
    RS, // Serbia
    SC, // Seychelles
    SL, // Sierra Leone
    SG, // Singapore
    SX, // Sint Maarten (Dutch part)
    SK, // Slovakia
    SI, // Slovenia
    SB, // Solomon Islands
    SO, // Somalia
    ZA, // South Africa
    GS, // South Georgia and the South Sandwich Islands
    SS, // South Sudan
    ES, // Spain
    LK, // Sri Lanka
    SD, // Sudan
    SR, // Suriname
    SJ, // Svalbard and Jan Mayen
    SE, // Sweden
    CH, // Switzerland
    SY, // Syrian Arab Republic
    TW, // Taiwan, Province of China
    TJ, // Tajikistan
    TZ, // Tanzania, United Republic of
    TH, // Thailand
    TL, // Timor-Leste
    TG, // Togo
    TK, // Tokelau
    TO, // Tonga
    TT, // Trinidad and Tobago
    TN, // Tunisia
    TR, // Turkey
    TM, // Turkmenistan
    TC, // Turks and Caicos Islands
    TV, // Tuvalu
    UG, // Uganda
    UA, // Ukraine
    AE, // United Arab Emirates
    GB, // United Kingdom of Great Britain and Northern Ireland
    US, // United States of America
    UM, // United States Minor Outlying Islands
    UY, // Uruguay
    UZ, // Uzbekistan
    VU, // Vanuatu
    VE, // Venezuela (Bolivarian Republic of)
    VN, // Viet Nam
    VG, // Virgin Islands (British)
    VI, // Virgin Islands (U.S.)
    WF, // Wallis and Futuna
    EH, // Western Sahara
    YE, // Yemen
    ZM, // Zambia
    ZW, // Zimbabwe
}
