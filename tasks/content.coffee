path = require 'path'
fs = require 'fs'
handlebars = require './../lib/helped_handlebars.coffee'
PageParser = require './../lib/page_parser'

module.exports = (grunt) ->
	grunt_helpers = require('grunt-lib-contrib').init(grunt);
	_ = grunt.util._

	grunt.registerMultiTask 'generate-html', 'Generate html files from the content files', ->
		options = @options
			basePath: false
			flatten: false


		@file.dest = path.normalize @file.dest

		page_parser = new PageParser
			templates_path: 'templates/'

		# read all the files specified in the task
		content_files = grunt.file.expand @file.src

		# for each task
		for content_file in content_files
			# nothing we can do if this is not a file
			continue unless grunt.file.isFile content_file

			page_data = page_parser.parse_file content_file

			grunt.verbose.writeln "Prepared data tree for #{content_file.cyan}"

			# we can only render pages that specify a template to do it with
			layout_path = "templates/#{page_data.meta.layout}"
			unless layout_path? and grunt.file.isFile layout_path
				grunt.log.error "Can't render page for #{content_file.cyan} because no layout has been defined"

			try
				template_src = grunt.file.read layout_path
				template = handlebars.compile template_src
			catch error
				grunt.log.error error
				grunt.fail.warn "Handlebars failed to compile '#{layout_path}'."

			grunt.verbose.writeln "Compiled layout #{layout_path.cyan}"

			# determine the file path of the rendered template file
			dest_file_path = grunt_helpers.buildIndividualDest @file.dest, content_file, options.basePath, options.flatten

			# render the template
			rendered_template = template page_data

			grunt.file.write dest_file_path, rendered_template
			grunt.log.writeln "File '#{dest_file_path.cyan}' created."