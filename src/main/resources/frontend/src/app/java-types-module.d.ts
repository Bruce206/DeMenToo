/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.20.583 on 2020-04-08 13:48:44.

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
    instances?: Instance[];
    lastMessage?: Date;
    timeAgo?: string;
}

export interface InstanceHealthMessage {
    instance?: Instance;
    status?: string;
    responseTime?: number;
}

export interface ApacheUrlConf {
    url?: string;
    https?: boolean;
    http?: boolean;
    filenames?: string[];
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

export type AppType = "SPRING_BOOT" | "OTHER";
