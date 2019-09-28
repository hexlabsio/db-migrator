import io.hexlabs.kloudformation.module.serverless.Serverless
import io.hexlabs.kloudformation.module.serverless.serverless
import io.kloudformation.KloudFormation
import io.kloudformation.StackBuilder
import io.kloudformation.json
import io.kloudformation.model.Output
import io.kloudformation.model.iam.IamPolicyVersion
import io.kloudformation.model.iam.actions
import io.kloudformation.model.iam.policyDocument
import io.kloudformation.model.iam.resource
import io.kloudformation.property.aws.iam.role.Policy
import io.kloudformation.resource.aws.ec2.securityGroup
import io.kloudformation.unaryPlus

class Stack : StackBuilder {

    override fun KloudFormation.create(args: List<String>) {
        val securityGroup = securityGroup(+"Database Migrator SG") { vpcId(+"vpc-35efcd53") }
        val codeLocation = args.first()
        val privateFunction = serverless("database-migrator-private", "live", +"hexlabs-deployments") {
            serverlessFunction(
                    functionId = "migrator",
                    codeLocationKey = +codeLocation,
                    handler = +"io.klouds.migrator.MigratorHandler",
                    runtime = +"java8",
                    privateConfig = Serverless.PrivateConfig(+listOf(securityGroup.GroupId()), +listOf(
                            +"subnet-a9d34ccf",
                            +"subnet-cfc11895",
                            +"subnet-c38de28b"
                    ))
            ) {
                lambdaFunction {
                    timeout(30)
                    memorySize(1024)
                }
            }
        }.functions.first().function
        val customResource = serverless("database-migrator-public", "live", +"hexlabs-deployments") {
            serverlessFunction(
                    functionId = "custom-resource",
                    codeLocationKey = +codeLocation,
                    handler = +"io.klouds.migrator.CustomResourceHandler",
                    runtime = +"java8"
            ) {
                globalRole {
                    policies(this.policies.orEmpty() + listOf(Policy(
                            policyName = +"function-access",
                            policyDocument = policyDocument(id = "function-access-policy", version = IamPolicyVersion.V2.version) {
                                statement(actions("lambda:InvokeFunction"), resource = resource(privateFunction.Arn()))
                            }
                    )))
                }
                lambdaFunction {
                    timeout(40)
                    memorySize(2048)
                    environment {
                        variables(json(mapOf(
                                "MIGRATOR_ARN" to privateFunction.Arn()
                        )))
                    }
                }
            }
        }.functions.first().function
        outputs(
                "MigratorArn" to Output(customResource.Arn(), export = Output.Export(+"DBMigratorArn"))
        )
    }
}