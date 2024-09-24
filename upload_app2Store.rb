require 'json'
require 'rest-client'

module Fastlane
  module Actions

    class UploadToApps2storeAction < Action
      def self.run(params)

        platform = "android"

        UI.user_error! "File not found" unless file = File.open(params["./app/build/outputs/apk/poynt/debug/app-poynt-debug.apk"])

        ##########################s
        ## Initialize variables ##
        ##########################
        userName = "PagoPA auto upload"
        x_access_key = "F50YH3GO5Y7I26TZG5VLWBZCDQ31YI64"
        os_id = ""
        project = ""
        baseUrl = "https://cloud.apps2store.it/v1/"
        release_notes = "Automatic build from fastLane"
        projectName = "SWC_SMARTPOS_POYNT"

        pagoPaUser = {
          'username' => "devops",
          'pwd' => "m0biles0ft"
        }.to_json

        ## LOGIN ##
        UI.message "Logging in..."
        begin
          login_request = RestClient.post  baseUrl + "authenticate", pagoPaUser, {content_type: :json, accept: :json}
        rescue RestClient::ExceptionWithResponse => err
          UI.error "Error logging in:"
          UI.user_error! err
        end

        login_parse = JSON.parse(login_request)

        UI.error login_parse["message"] and return unless login_parse["success"] == true

        UI.success "You are now logged as " +userName
        user = login_parse["user"]
        user_id = user["_id"]
        token = login_parse["token"]

        UI.message "User Id: #{user_id}"

        ## GET OS ##
        UI.message "Fetching OS..."

        begin
          os_request = RestClient.get(baseUrl + "getOssystem", headers={content_type: :json, accept: :json, :'x-access-key' => x_access_key , :'x-access-token' => token})
        rescue RestClient::ExceptionWithResponse => err
          UI.error "Error fetching OS:"
          UI.user_error! err
        end

        os_id = JSON.parse(os_request.body)["ossystems"].find { |os| os["type"] == platform.to_s.upcase }["_id"]

        UI.message "OS ID: " + os_id

        ## GET APPS ##
        uri_get_apps = baseUrl+"getApps?usersadmin=#{user_id}"

        begin
          app_request = RestClient.get(uri_get_apps, headers={content_type: :json, accept: :json,:'x-access-key' => x_access_key,:'x-access-token' => token})
        rescue => err
          UI.error "Error fetching app:"
          UI.user_error! err
        end

        project = JSON.parse(app_request.body)["apps"].find { |app| app["name"] == projectName }

        UI.user_error! "Project not found!" and return unless project #&& is_active == true

        # TODO check
        app_id = project["_id"]

        ## CREATE VERSION ##
        mock_app = {
          "appid" => app_id,
          "osSystemControl" => os_id,
          "os_system" => os_id,
          "permissions" => [],
          "permissionsControl" => [],
          "release_notes" => release_notes,
          "uploader" => user_id
        }.to_json

        begin
          create_version_request = RestClient.post baseUrl + "createVersion", mock_app, {content_type: :json, accept: :json,:'x-access-key' => x_access_key ,:'x-access-token' => token}
        rescue => err
          UI.error "Error creating version:"
          UI.user_error! err
        end
        create_version_parse = JSON.parse(create_version_request.body)
        version = create_version_parse["version"]
        message = create_version_parse['message']

        version_data = {
          "versionid" => version,
          "title" => projectName,
          "subtitle" => "",
          "versionname" => "",
          "isActive" => "true",
          "ostype" => os_id,
          "typeUpload" => "appFile",
          "release_notes" => release_notes,
          "file" => file
        }

        begin
          upload_request = RestClient.post baseUrl+"uploadFile", version_data, {:'x-access-key' => x_access_key ,:'x-access-token' => token}
        rescue => err
          UI.error "Error uploading:"
          UI.user_error! err
        end

        upload_parse = JSON.parse(upload_request.body)
        UI.message upload_parse['message']
        UI.message upload_parse['binarypath']
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        "Deploys to Apps2Store"
      end

      def self.details
        # Optional:
        # this is your chance to provide a more detailed description of this action
        ""
      end

      def self.return_value
        # If your method provides a return value, you can describe here what it does
      end

      def self.authors
        # So no one will ever forget your contribution to fastlane :) You are awesome btw!
        ["Carlo De Chellis for pagoPA"]
      end

    end
  end
end
