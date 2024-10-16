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
 * @interface SelfServiceAccountManagementSettings
 */
export interface SelfServiceAccountManagementSettings {
    /**
     * 
     * @type {boolean}
     * @memberof SelfServiceAccountManagementSettings
     */
    enabled?: boolean;
}

export function SelfServiceAccountManagementSettingsFromJSON(json: any): SelfServiceAccountManagementSettings {
    return SelfServiceAccountManagementSettingsFromJSONTyped(json, false);
}

export function SelfServiceAccountManagementSettingsFromJSONTyped(json: any, ignoreDiscriminator: boolean): SelfServiceAccountManagementSettings {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'enabled': !exists(json, 'enabled') ? undefined : json['enabled'],
    };
}

export function SelfServiceAccountManagementSettingsToJSON(value?: SelfServiceAccountManagementSettings | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'enabled': value.enabled,
    };
}

