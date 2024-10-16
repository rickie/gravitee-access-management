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


import * as runtime from '../runtime';
import {
    NewReporter,
    NewReporterFromJSON,
    NewReporterToJSON,
    Reporter,
    ReporterFromJSON,
    ReporterToJSON,
    UpdateReporter,
    UpdateReporterFromJSON,
    UpdateReporterToJSON,
} from '../models';

export interface Create8Request {
    organizationId: string;
    environmentId: string;
    domain: string;
    body?: NewReporter;
}

export interface Delete11Request {
    organizationId: string;
    environmentId: string;
    domain: string;
    reporter: string;
}

export interface Get16Request {
    organizationId: string;
    environmentId: string;
    domain: string;
    reporter: string;
}

export interface Get31Request {
    reporter: string;
}

export interface GetSchema5Request {
    reporter: string;
}

export interface List18Request {
    organizationId: string;
    environmentId: string;
    domain: string;
    userProvider?: boolean;
}

export interface Update8Request {
    organizationId: string;
    environmentId: string;
    domain: string;
    reporter: string;
    reporter2: UpdateReporter;
}

/**
 * 
 */
export class ReporterApi extends runtime.BaseAPI {

    /**
     * User must have the DOMAIN_REPORTER[CREATE] permission on the specified domain or DOMAIN_REPORTER[CREATE] permission on the specified environment or DOMAIN_REPORTER[CREATE] permission on the specified organization.
     * Create a reporter for a security domain
     */
    async create8Raw(requestParameters: Create8Request, initOverrides?: RequestInit | runtime.InitOverideFunction): Promise<runtime.ApiResponse<Reporter>> {
        if (requestParameters.organizationId === null || requestParameters.organizationId === undefined) {
            throw new runtime.RequiredError('organizationId','Required parameter requestParameters.organizationId was null or undefined when calling create8.');
        }

        if (requestParameters.environmentId === null || requestParameters.environmentId === undefined) {
            throw new runtime.RequiredError('environmentId','Required parameter requestParameters.environmentId was null or undefined when calling create8.');
        }

        if (requestParameters.domain === null || requestParameters.domain === undefined) {
            throw new runtime.RequiredError('domain','Required parameter requestParameters.domain was null or undefined when calling create8.');
        }

        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        headerParameters['Content-Type'] = 'application/json';

        if (this.configuration && this.configuration.apiKey) {
            headerParameters["Authorization"] = this.configuration.apiKey("Authorization"); // gravitee-auth authentication
        }

        const response = await this.request({
            path: `/organizations/{organizationId}/environments/{environmentId}/domains/{domain}/reporters`.replace(`{${"organizationId"}}`, encodeURIComponent(String(requestParameters.organizationId))).replace(`{${"environmentId"}}`, encodeURIComponent(String(requestParameters.environmentId))).replace(`{${"domain"}}`, encodeURIComponent(String(requestParameters.domain))),
            method: 'POST',
            headers: headerParameters,
            query: queryParameters,
            body: NewReporterToJSON(requestParameters.body),
        }, initOverrides);

        return new runtime.JSONApiResponse(response, (jsonValue) => ReporterFromJSON(jsonValue));
    }

    /**
     * User must have the DOMAIN_REPORTER[CREATE] permission on the specified domain or DOMAIN_REPORTER[CREATE] permission on the specified environment or DOMAIN_REPORTER[CREATE] permission on the specified organization.
     * Create a reporter for a security domain
     */
    async create8(requestParameters: Create8Request, initOverrides?: RequestInit | runtime.InitOverideFunction): Promise<Reporter> {
        const response = await this.create8Raw(requestParameters, initOverrides);
        return await response.value();
    }

    /**
     * User must have the DOMAIN_REPORTER[DELETE] permission on the specified domain or DOMAIN_REPORTER[DELETE] permission on the specified environment or DOMAIN_REPORTER[DELETE] permission on the specified organization
     * Delete a reporter
     */
    async delete11Raw(requestParameters: Delete11Request, initOverrides?: RequestInit | runtime.InitOverideFunction): Promise<runtime.ApiResponse<void>> {
        if (requestParameters.organizationId === null || requestParameters.organizationId === undefined) {
            throw new runtime.RequiredError('organizationId','Required parameter requestParameters.organizationId was null or undefined when calling delete11.');
        }

        if (requestParameters.environmentId === null || requestParameters.environmentId === undefined) {
            throw new runtime.RequiredError('environmentId','Required parameter requestParameters.environmentId was null or undefined when calling delete11.');
        }

        if (requestParameters.domain === null || requestParameters.domain === undefined) {
            throw new runtime.RequiredError('domain','Required parameter requestParameters.domain was null or undefined when calling delete11.');
        }

        if (requestParameters.reporter === null || requestParameters.reporter === undefined) {
            throw new runtime.RequiredError('reporter','Required parameter requestParameters.reporter was null or undefined when calling delete11.');
        }

        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        if (this.configuration && this.configuration.apiKey) {
            headerParameters["Authorization"] = this.configuration.apiKey("Authorization"); // gravitee-auth authentication
        }

        const response = await this.request({
            path: `/organizations/{organizationId}/environments/{environmentId}/domains/{domain}/reporters/{reporter}`.replace(`{${"organizationId"}}`, encodeURIComponent(String(requestParameters.organizationId))).replace(`{${"environmentId"}}`, encodeURIComponent(String(requestParameters.environmentId))).replace(`{${"domain"}}`, encodeURIComponent(String(requestParameters.domain))).replace(`{${"reporter"}}`, encodeURIComponent(String(requestParameters.reporter))),
            method: 'DELETE',
            headers: headerParameters,
            query: queryParameters,
        }, initOverrides);

        return new runtime.VoidApiResponse(response);
    }

    /**
     * User must have the DOMAIN_REPORTER[DELETE] permission on the specified domain or DOMAIN_REPORTER[DELETE] permission on the specified environment or DOMAIN_REPORTER[DELETE] permission on the specified organization
     * Delete a reporter
     */
    async delete11(requestParameters: Delete11Request, initOverrides?: RequestInit | runtime.InitOverideFunction): Promise<void> {
        await this.delete11Raw(requestParameters, initOverrides);
    }

    /**
     * User must have the DOMAIN_REPORTER[READ] permission on the specified domain or DOMAIN_REPORTER[READ] permission on the specified environment or DOMAIN_REPORTER[READ] permission on the specified organization
     * Get a reporter
     */
    async get16Raw(requestParameters: Get16Request, initOverrides?: RequestInit | runtime.InitOverideFunction): Promise<runtime.ApiResponse<Reporter>> {
        if (requestParameters.organizationId === null || requestParameters.organizationId === undefined) {
            throw new runtime.RequiredError('organizationId','Required parameter requestParameters.organizationId was null or undefined when calling get16.');
        }

        if (requestParameters.environmentId === null || requestParameters.environmentId === undefined) {
            throw new runtime.RequiredError('environmentId','Required parameter requestParameters.environmentId was null or undefined when calling get16.');
        }

        if (requestParameters.domain === null || requestParameters.domain === undefined) {
            throw new runtime.RequiredError('domain','Required parameter requestParameters.domain was null or undefined when calling get16.');
        }

        if (requestParameters.reporter === null || requestParameters.reporter === undefined) {
            throw new runtime.RequiredError('reporter','Required parameter requestParameters.reporter was null or undefined when calling get16.');
        }

        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        if (this.configuration && this.configuration.apiKey) {
            headerParameters["Authorization"] = this.configuration.apiKey("Authorization"); // gravitee-auth authentication
        }

        const response = await this.request({
            path: `/organizations/{organizationId}/environments/{environmentId}/domains/{domain}/reporters/{reporter}`.replace(`{${"organizationId"}}`, encodeURIComponent(String(requestParameters.organizationId))).replace(`{${"environmentId"}}`, encodeURIComponent(String(requestParameters.environmentId))).replace(`{${"domain"}}`, encodeURIComponent(String(requestParameters.domain))).replace(`{${"reporter"}}`, encodeURIComponent(String(requestParameters.reporter))),
            method: 'GET',
            headers: headerParameters,
            query: queryParameters,
        }, initOverrides);

        return new runtime.JSONApiResponse(response, (jsonValue) => ReporterFromJSON(jsonValue));
    }

    /**
     * User must have the DOMAIN_REPORTER[READ] permission on the specified domain or DOMAIN_REPORTER[READ] permission on the specified environment or DOMAIN_REPORTER[READ] permission on the specified organization
     * Get a reporter
     */
    async get16(requestParameters: Get16Request, initOverrides?: RequestInit | runtime.InitOverideFunction): Promise<Reporter> {
        const response = await this.get16Raw(requestParameters, initOverrides);
        return await response.value();
    }

    /**
     * There is no particular permission needed. User must be authenticated.
     * Get a reporter plugin
     */
    async get31Raw(requestParameters: Get31Request, initOverrides?: RequestInit | runtime.InitOverideFunction): Promise<runtime.ApiResponse<void>> {
        if (requestParameters.reporter === null || requestParameters.reporter === undefined) {
            throw new runtime.RequiredError('reporter','Required parameter requestParameters.reporter was null or undefined when calling get31.');
        }

        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        if (this.configuration && this.configuration.apiKey) {
            headerParameters["Authorization"] = this.configuration.apiKey("Authorization"); // gravitee-auth authentication
        }

        const response = await this.request({
            path: `/platform/plugins/reporters/{reporter}`.replace(`{${"reporter"}}`, encodeURIComponent(String(requestParameters.reporter))),
            method: 'GET',
            headers: headerParameters,
            query: queryParameters,
        }, initOverrides);

        return new runtime.VoidApiResponse(response);
    }

    /**
     * There is no particular permission needed. User must be authenticated.
     * Get a reporter plugin
     */
    async get31(requestParameters: Get31Request, initOverrides?: RequestInit | runtime.InitOverideFunction): Promise<void> {
        await this.get31Raw(requestParameters, initOverrides);
    }

    /**
     * Get a reporter plugin\'s schema
     */
    async getSchema5Raw(requestParameters: GetSchema5Request, initOverrides?: RequestInit | runtime.InitOverideFunction): Promise<runtime.ApiResponse<void>> {
        if (requestParameters.reporter === null || requestParameters.reporter === undefined) {
            throw new runtime.RequiredError('reporter','Required parameter requestParameters.reporter was null or undefined when calling getSchema5.');
        }

        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        if (this.configuration && this.configuration.apiKey) {
            headerParameters["Authorization"] = this.configuration.apiKey("Authorization"); // gravitee-auth authentication
        }

        const response = await this.request({
            path: `/platform/plugins/reporters/{reporter}/schema`.replace(`{${"reporter"}}`, encodeURIComponent(String(requestParameters.reporter))),
            method: 'GET',
            headers: headerParameters,
            query: queryParameters,
        }, initOverrides);

        return new runtime.VoidApiResponse(response);
    }

    /**
     * Get a reporter plugin\'s schema
     */
    async getSchema5(requestParameters: GetSchema5Request, initOverrides?: RequestInit | runtime.InitOverideFunction): Promise<void> {
        await this.getSchema5Raw(requestParameters, initOverrides);
    }

    /**
     * User must have the DOMAIN_REPORTER[LIST] permission on the specified domain or DOMAIN_REPORTER[LIST] permission on the specified environment or DOMAIN_REPORTER[LIST] permission on the specified organization. Except if user has DOMAIN_REPORTER[READ] permission on the domain, environment or organization, each returned reporter is filtered and contains only basic information such as id and name and type.
     * List registered reporters for a security domain
     */
    async list18Raw(requestParameters: List18Request, initOverrides?: RequestInit | runtime.InitOverideFunction): Promise<runtime.ApiResponse<Array<Reporter>>> {
        if (requestParameters.organizationId === null || requestParameters.organizationId === undefined) {
            throw new runtime.RequiredError('organizationId','Required parameter requestParameters.organizationId was null or undefined when calling list18.');
        }

        if (requestParameters.environmentId === null || requestParameters.environmentId === undefined) {
            throw new runtime.RequiredError('environmentId','Required parameter requestParameters.environmentId was null or undefined when calling list18.');
        }

        if (requestParameters.domain === null || requestParameters.domain === undefined) {
            throw new runtime.RequiredError('domain','Required parameter requestParameters.domain was null or undefined when calling list18.');
        }

        const queryParameters: any = {};

        if (requestParameters.userProvider !== undefined) {
            queryParameters['userProvider'] = requestParameters.userProvider;
        }

        const headerParameters: runtime.HTTPHeaders = {};

        if (this.configuration && this.configuration.apiKey) {
            headerParameters["Authorization"] = this.configuration.apiKey("Authorization"); // gravitee-auth authentication
        }

        const response = await this.request({
            path: `/organizations/{organizationId}/environments/{environmentId}/domains/{domain}/reporters`.replace(`{${"organizationId"}}`, encodeURIComponent(String(requestParameters.organizationId))).replace(`{${"environmentId"}}`, encodeURIComponent(String(requestParameters.environmentId))).replace(`{${"domain"}}`, encodeURIComponent(String(requestParameters.domain))),
            method: 'GET',
            headers: headerParameters,
            query: queryParameters,
        }, initOverrides);

        return new runtime.JSONApiResponse(response, (jsonValue) => jsonValue.map(ReporterFromJSON));
    }

    /**
     * User must have the DOMAIN_REPORTER[LIST] permission on the specified domain or DOMAIN_REPORTER[LIST] permission on the specified environment or DOMAIN_REPORTER[LIST] permission on the specified organization. Except if user has DOMAIN_REPORTER[READ] permission on the domain, environment or organization, each returned reporter is filtered and contains only basic information such as id and name and type.
     * List registered reporters for a security domain
     */
    async list18(requestParameters: List18Request, initOverrides?: RequestInit | runtime.InitOverideFunction): Promise<Array<Reporter>> {
        const response = await this.list18Raw(requestParameters, initOverrides);
        return await response.value();
    }

    /**
     * There is no particular permission needed. User must be authenticated.
     * List reporter plugins
     */
    async list31Raw(initOverrides?: RequestInit | runtime.InitOverideFunction): Promise<runtime.ApiResponse<void>> {
        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        if (this.configuration && this.configuration.apiKey) {
            headerParameters["Authorization"] = this.configuration.apiKey("Authorization"); // gravitee-auth authentication
        }

        const response = await this.request({
            path: `/platform/plugins/reporters`,
            method: 'GET',
            headers: headerParameters,
            query: queryParameters,
        }, initOverrides);

        return new runtime.VoidApiResponse(response);
    }

    /**
     * There is no particular permission needed. User must be authenticated.
     * List reporter plugins
     */
    async list31(initOverrides?: RequestInit | runtime.InitOverideFunction): Promise<void> {
        await this.list31Raw(initOverrides);
    }

    /**
     * User must have the DOMAIN_REPORTER[UPDATE] permission on the specified domain or DOMAIN_REPORTER[UPDATE] permission on the specified environment or DOMAIN_REPORTER[UPDATE] permission on the specified organization
     * Update a reporter
     */
    async update8Raw(requestParameters: Update8Request, initOverrides?: RequestInit | runtime.InitOverideFunction): Promise<runtime.ApiResponse<Reporter>> {
        if (requestParameters.organizationId === null || requestParameters.organizationId === undefined) {
            throw new runtime.RequiredError('organizationId','Required parameter requestParameters.organizationId was null or undefined when calling update8.');
        }

        if (requestParameters.environmentId === null || requestParameters.environmentId === undefined) {
            throw new runtime.RequiredError('environmentId','Required parameter requestParameters.environmentId was null or undefined when calling update8.');
        }

        if (requestParameters.domain === null || requestParameters.domain === undefined) {
            throw new runtime.RequiredError('domain','Required parameter requestParameters.domain was null or undefined when calling update8.');
        }

        if (requestParameters.reporter === null || requestParameters.reporter === undefined) {
            throw new runtime.RequiredError('reporter','Required parameter requestParameters.reporter was null or undefined when calling update8.');
        }

        if (requestParameters.reporter2 === null || requestParameters.reporter2 === undefined) {
            throw new runtime.RequiredError('reporter2','Required parameter requestParameters.reporter2 was null or undefined when calling update8.');
        }

        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        headerParameters['Content-Type'] = 'application/json';

        if (this.configuration && this.configuration.apiKey) {
            headerParameters["Authorization"] = this.configuration.apiKey("Authorization"); // gravitee-auth authentication
        }

        const response = await this.request({
            path: `/organizations/{organizationId}/environments/{environmentId}/domains/{domain}/reporters/{reporter}`.replace(`{${"organizationId"}}`, encodeURIComponent(String(requestParameters.organizationId))).replace(`{${"environmentId"}}`, encodeURIComponent(String(requestParameters.environmentId))).replace(`{${"domain"}}`, encodeURIComponent(String(requestParameters.domain))).replace(`{${"reporter"}}`, encodeURIComponent(String(requestParameters.reporter))),
            method: 'PUT',
            headers: headerParameters,
            query: queryParameters,
            body: UpdateReporterToJSON(requestParameters.reporter2),
        }, initOverrides);

        return new runtime.JSONApiResponse(response, (jsonValue) => ReporterFromJSON(jsonValue));
    }

    /**
     * User must have the DOMAIN_REPORTER[UPDATE] permission on the specified domain or DOMAIN_REPORTER[UPDATE] permission on the specified environment or DOMAIN_REPORTER[UPDATE] permission on the specified organization
     * Update a reporter
     */
    async update8(requestParameters: Update8Request, initOverrides?: RequestInit | runtime.InitOverideFunction): Promise<Reporter> {
        const response = await this.update8Raw(requestParameters, initOverrides);
        return await response.value();
    }

}
