/* tslint:disable */
/* eslint-disable */
/**
 * Gravitee.io - Access Management API
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

import { exists, mapValues } from '../runtime';
/**
 * 
 * @export
 * @interface PatchAlertTrigger
 */
export interface PatchAlertTrigger {
    /**
     * 
     * @type {boolean}
     * @memberof PatchAlertTrigger
     */
    enabled?: boolean;
    /**
     * 
     * @type {Array<string>}
     * @memberof PatchAlertTrigger
     */
    alertNotifiers?: Array<string>;
    /**
     * 
     * @type {string}
     * @memberof PatchAlertTrigger
     */
    type: PatchAlertTriggerTypeEnum;
}


/**
 * @export
 */
export const PatchAlertTriggerTypeEnum = {
    TooManyLoginFailures: 'TOO_MANY_LOGIN_FAILURES',
    RiskAssessment: 'RISK_ASSESSMENT'
} as const;
export type PatchAlertTriggerTypeEnum = typeof PatchAlertTriggerTypeEnum[keyof typeof PatchAlertTriggerTypeEnum];


export function PatchAlertTriggerFromJSON(json: any): PatchAlertTrigger {
    return PatchAlertTriggerFromJSONTyped(json, false);
}

export function PatchAlertTriggerFromJSONTyped(json: any, ignoreDiscriminator: boolean): PatchAlertTrigger {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'enabled': !exists(json, 'enabled') ? undefined : json['enabled'],
        'alertNotifiers': !exists(json, 'alertNotifiers') ? undefined : json['alertNotifiers'],
        'type': json['type'],
    };
}

export function PatchAlertTriggerToJSON(value?: PatchAlertTrigger | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'enabled': value.enabled,
        'alertNotifiers': value.alertNotifiers,
        'type': value.type,
    };
}

