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
 * @interface PatchApplicationAdvancedSettings
 */
export interface PatchApplicationAdvancedSettings {
    /**
     * 
     * @type {boolean}
     * @memberof PatchApplicationAdvancedSettings
     */
    skipConsent?: boolean;
    /**
     * 
     * @type {boolean}
     * @memberof PatchApplicationAdvancedSettings
     */
    flowsInherited?: boolean;
}

export function PatchApplicationAdvancedSettingsFromJSON(json: any): PatchApplicationAdvancedSettings {
    return PatchApplicationAdvancedSettingsFromJSONTyped(json, false);
}

export function PatchApplicationAdvancedSettingsFromJSONTyped(json: any, ignoreDiscriminator: boolean): PatchApplicationAdvancedSettings {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'skipConsent': !exists(json, 'skipConsent') ? undefined : json['skipConsent'],
        'flowsInherited': !exists(json, 'flowsInherited') ? undefined : json['flowsInherited'],
    };
}

export function PatchApplicationAdvancedSettingsToJSON(value?: PatchApplicationAdvancedSettings | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'skipConsent': value.skipConsent,
        'flowsInherited': value.flowsInherited,
    };
}

