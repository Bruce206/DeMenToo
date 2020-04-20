/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.21.588 on 2020-04-20 17:53:16.

export interface Server extends MonitoredSuperEntity {
    id?: number;
    serverName?: string;
    ip?: string;
    blacklisted?: boolean;
    whitelisted?: boolean;
    activeCheckDisabled?: boolean;
    hoster?: string;
    displayName?: string;
    customer?: string;
    apacheConfs?: ApacheUrlConf[];
    xibisOneDomains?: XibisOneDomain[];
    combinedDomains?: CombinedDomainContainer[];
    instances?: Instance[];
    lastMessage?: Date;
    timeAgo?: string;
}

export interface InstanceHealthMessage {
    instance?: Instance;
    status?: string;
    responseTime?: number;
}

export interface ApacheUrlConf extends DomainContainer {
    filenames?: string[];
}

export interface XibisOneDomain extends DomainContainer {
    node?: string;
    database?: string;
}

export interface CombinedDomainContainer extends DomainContainer {
    pingStatus?: PingStatus;
    actualServerName?: string;
    inApache?: boolean;
    inXibisOne?: boolean;
}

export interface Instance extends MonitoredSuperEntity {
    id?: number;
    identifier?: string;
    version?: string;
    licensedFor?: string;
    port?: string;
    excludeFromHealthcheck?: boolean;
    instanceType?: InstanceType;
    type?: string;
    prod?: boolean;
    domains?: Domain[];
    details?: InstanceDetail[];
    server?: Server;
    lastMessage?: Date;
    timeAgo?: string;
    lastMessageCritical?: boolean;
    instanceDetailsByKey?: { [index: string]: InstanceDetail[] };
    instanceDetailsByCategory?: { [index: string]: InstanceDetail[] };
}

export interface MonitoredSuperEntity {
    created?: Date;
    modified?: Date;
}

export interface DomainContainer {
    url?: string;
    ip?: string;
    https?: boolean;
    http?: boolean;
    serverId?: number;
    serverName?: string;
}

export interface InstanceType {
    id?: number;
    name?: string;
    image?: any;
    updateFileName?: string;
    updatePath?: string;
    updateTime?: Date;
    appType?: AppType;
    healthUrl?: string;
    apacheTemplate?: string;
    applicationPropertiesTemplate?: string;
    serviceTemplate?: string;
    certbot?: boolean;
    databaseNameTemplate?: string;
    messageInterval?: number;
    instances?: Instance[];
    instanceDetailKeys?: string[];
}

export interface Domain extends MonitoredSuperEntity {
    id?: number;
    name?: string;
    url?: string;
}

export interface InstanceDetail {
    id?: number;
    category?: string;
    key?: string;
    value?: string;
}

export type PingStatus = "SAME_SERVER" | "OTHER_SERVER" | "FOREIGN_SERVER" | "UNKNOWN_HOST";

export type AppType = "SPRING_BOOT" | "OTHER";
