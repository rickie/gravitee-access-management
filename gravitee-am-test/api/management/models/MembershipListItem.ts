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
import {
    Membership,
    MembershipFromJSON,
    MembershipFromJSONTyped,
    MembershipToJSON,
} from './Membership';

/**
 * 
 * @export
 * @interface MembershipListItem
 */
export interface MembershipListItem {
    /**
     * 
     * @type {Array<Membership>}
     * @memberof MembershipListItem
     */
    memberships?: Array<Membership>;
    /**
     * 
     * @type {{ [key: string]: { [key: string]: any; }; }}
     * @memberof MembershipListItem
     */
    metadata?: { [key: string]: { [key: string]: any; }; };
}

export function MembershipListItemFromJSON(json: any): MembershipListItem {
    return MembershipListItemFromJSONTyped(json, false);
}

export function MembershipListItemFromJSONTyped(json: any, ignoreDiscriminator: boolean): MembershipListItem {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'memberships': !exists(json, 'memberships') ? undefined : ((json['memberships'] as Array<any>).map(MembershipFromJSON)),
        'metadata': !exists(json, 'metadata') ? undefined : json['metadata'],
    };
}

export function MembershipListItemToJSON(value?: MembershipListItem | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'memberships': value.memberships === undefined ? undefined : ((value.memberships as Array<any>).map(MembershipToJSON)),
        'metadata': value.metadata,
    };
}

