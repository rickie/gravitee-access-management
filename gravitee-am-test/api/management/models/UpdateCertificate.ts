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
 * @interface UpdateCertificate
 */
export interface UpdateCertificate {
    /**
     * 
     * @type {string}
     * @memberof UpdateCertificate
     */
    name: string;
    /**
     * 
     * @type {string}
     * @memberof UpdateCertificate
     */
    configuration: string;
}

export function UpdateCertificateFromJSON(json: any): UpdateCertificate {
    return UpdateCertificateFromJSONTyped(json, false);
}

export function UpdateCertificateFromJSONTyped(json: any, ignoreDiscriminator: boolean): UpdateCertificate {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'name': json['name'],
        'configuration': json['configuration'],
    };
}

export function UpdateCertificateToJSON(value?: UpdateCertificate | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'name': value.name,
        'configuration': value.configuration,
    };
}

