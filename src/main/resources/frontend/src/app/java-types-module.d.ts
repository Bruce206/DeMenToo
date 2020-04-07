/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.20.583 on 2020-03-30 14:57:29.

export interface Customer extends AuditorAware {
    id?: number;
    customerNumber?: string;
    currentPaymentId?: number;
    riskCarrierId?: number;
    salutation?: string;
    titular?: string;
    use_last_name?: boolean;
    forname?: string;
    formal_backup?: string;
    surname?: string;
    surname_backup?: string;
    nickname?: string;
    care_of?: string;
    address?: string;
    postalCode?: string;
    city?: string;
    ownContributionChangeDate?: Date;
    insuranceReportAmount?: number;
    insuranceReportAmountBackup?: number;
    latestInvoice?: Date;
    secondLatestInvoice?: Date;
    antepenultimateInvoice?: Date;
    revocationDate?: Date;
    latestConfirmationDate?: Date;
    externalCopies?: boolean;
    screeningId?: number;
    country?: string;
    language?: string;
    changedSinceLastConfirmation?: boolean;
    account?: Account;
    contact?: Contact;
    data?: Data;
    info?: Info;
    invoicing?: Invoicing;
    premium?: Premium;
    services?: Services;
    instruments?: Instrument[];
    confirmationLogs?: ConfirmationLog[];
    displayName?: string;
    company?: boolean;
}

export interface PostalCode {
    id?: number;
    country?: string;
    postalCode?: string;
    city?: string;
}

export interface User extends AuditorAware {
    id?: number;
    firstName?: string;
    lastName?: string;
    loginName?: string;
    password?: string;
    role?: string;
    permissions?: Permission[];
    loginRetries?: number;
    loginRetriesLockDateTime?: Date;
    loginRetriesLockDuration?: number;
    locked?: boolean;
    lockedReason?: string;
    properties?: { [index: string]: any };
    displayName?: string;
    permissionsByTarget?: { [index: string]: Permission };
    loginRetriesLockUntil?: Date;
    email?: string;
    lockedByRetries?: boolean;
}

export interface MailTemplate extends AuditorAware {
    id?: number;
    name?: string;
    subject?: string;
    text?: string;
    priority?: string;
}

export interface Account {
    accountHolder?: string;
    accountHolderBackup?: string;
    accountNumber?: string;
    accountNumberBackup?: string;
    bic?: string;
    bicBackup?: string;
    bank?: string;
    bankCountry?: string;
    customerBankIbanOrBicChange?: boolean;
    sepaMandateDate?: Date;
    sepaSuffix?: Date;
    paymentCollection?: boolean;
}

export interface Contact {
    primary_email?: string;
    secondary_email?: string;
    faxNumber?: string;
    privatePhone?: string;
    mobilPhone?: string;
    additionalPhone?: string;
    currentMailSelection?: boolean;
}

export interface Data {
    entryDate?: Date;
    leaveDate?: Date;
    leaveWithoutRefund?: boolean;
    notificationChangeInTariff?: Date;
    latestScreening?: Date;
}

export interface Info {
    comment?: string;
}

export interface Invoicing {
    insuranceContribution?: number;
    newInsuranceContribution?: number;
    paymentCollectionNotBefore?: Date;
    paymentCollectionDate?: Date;
    fee?: number;
    feeReason?: string;
    fixedFee?: number;
    fixedFeeReason?: string;
    latestPaymentCollectionValue?: number;
    latestRefund?: number;
    latestPremium?: number;
    latestPamentDifference?: number;
}

export interface Premium {
    recommendedBy?: Customer;
    premiumRate?: number;
    courtage_share?: number;
}

export interface Services {
    groupService?: boolean;
    withoutOwnContributionNew?: boolean;
    withoutOwnContribution?: boolean;
}

export interface Instrument extends AuditorAware {
    id?: number;
    name?: string;
    specialCondition?: number;
    manufacturer?: string;
    manufacturingYear?: string;
    valueGrowth?: number;
    description?: string;
    groupName?: string;
    begin?: Date;
    terminationDate?: Date;
    instrumentCategory?: InstrumentCategory;
    deductions?: InstrumentDeduction[];
    options?: InstrumentInstrumentOption[];
    insuranceSums?: InstrumentInsuranceSum[];
    amount?: number;
    changedSinceLastConfirmation?: boolean;
    importRowNumber?: number;
}

export interface ConfirmationLog extends AuditorAware {
    id?: number;
    customer?: Customer;
    fileName?: string;
}

export interface AuditorAware {
    createdAt?: Date;
    updatedAt?: Date;
    createdBy?: string;
    lastModifiedBy?: string;
}

export interface Permission extends AuditorAware {
    id?: number;
    target?: string;
    level?: number;
}

export interface InstrumentCategory {
    id?: number;
    name?: string;
    specialConditions?: boolean;
}

export interface InstrumentDeduction extends AuditorAware {
    id?: number;
    value?: number;
    from?: Date;
    to?: Date;
}

export interface InstrumentInstrumentOption extends AuditorAware {
    id?: number;
    from?: Date;
    to?: Date;
    instrumentOption?: InstrumentOption;
}

export interface InstrumentInsuranceSum extends AuditorAware {
    id?: number;
    value?: number;
    from?: Date;
    to?: Date;
}

export interface InstrumentOption extends AuditorAware {
    id?: number;
    title?: string;
    title_short?: string;
    allow_group_selection?: boolean;
}
